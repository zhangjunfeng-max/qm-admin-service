package com.qm.admin.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qm.admin.common.constants.Constants;
import com.qm.admin.common.exception.BusinessException;
import com.qm.admin.common.response.CommonResultCode;
import com.qm.admin.system.config.SystemAuthProperties;
import com.qm.admin.system.dto.AuthenticatedSession;
import com.qm.admin.system.entity.SystemAuthToken;
import com.qm.admin.system.entity.SystemTenant;
import com.qm.admin.system.entity.SystemUser;
import com.qm.admin.system.entity.SystemUserTenant;
import com.qm.admin.system.mapper.SystemAuthTokenMapper;
import com.qm.admin.system.mapper.SystemTenantMapper;
import com.qm.admin.system.mapper.SystemUserMapper;
import com.qm.admin.system.mapper.SystemUserTenantMapper;
import com.qm.admin.system.security.SystemPrincipal;
import com.qm.admin.system.security.RsaPasswordDecryptor;
import com.qm.admin.system.security.TenantIsolation;
import com.qm.admin.system.util.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zjf
 */
@Service
public class SystemAuthService {

    private static final int TOKEN_BYTES = 32;

    private final SystemUserMapper userMapper;
    private final SystemTenantMapper tenantMapper;
    private final SystemUserTenantMapper userTenantMapper;
    private final SystemAuthTokenMapper tokenMapper;
    private final AuthSessionCache authSessionCache;
    private final PasswordEncoder passwordEncoder;
    private final SystemAuthProperties authProperties;
    private final RsaPasswordDecryptor rsaPasswordDecryptor;
    private final SecureRandom secureRandom = new SecureRandom();

    public SystemAuthService(SystemUserMapper userMapper, SystemTenantMapper tenantMapper,
                             SystemUserTenantMapper userTenantMapper, SystemAuthTokenMapper tokenMapper,
                             AuthSessionCache authSessionCache, PasswordEncoder passwordEncoder,
                             SystemAuthProperties authProperties, RsaPasswordDecryptor rsaPasswordDecryptor) {
        this.userMapper = userMapper;
        this.tenantMapper = tenantMapper;
        this.userTenantMapper = userTenantMapper;
        this.tokenMapper = tokenMapper;
        this.authSessionCache = authSessionCache;
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
        this.rsaPasswordDecryptor = rsaPasswordDecryptor;
    }

    //$2a$10$b2DEPlrwDy.lHXrbFY4jketMRkkVCAYMHsMc1.WlKhJlzw1SOdN.S
    @Transactional
    public TokenPair login(String username, String password, Long requestedTenantId) {
        return TenantIsolation.withoutTenant(() -> {
            String plainPassword = authProperties.isRsaPasswordEnabled()
                    ? rsaPasswordDecryptor.decrypt(password)
                    : password;
            SystemUser user = userMapper.selectOne(Wrappers.<SystemUser>lambdaQuery()
                    .eq(SystemUser::getUsername, username)
                    .eq(SystemUser::getStatus, Constants.YES)
                    .last("LIMIT 1"));
            if (user == null || !passwordEncoder.matches(plainPassword, user.getPasswordHash())) {
                throw new BusinessException(CommonResultCode.UNAUTHORIZED, "用户名或密码错误");
            }
            Long tenantId = selectLoginTenant(user.getId(), requestedTenantId);
            return createTokenPair(user.getId(), tenantId, user.getUsername());
        });
    }

    @Transactional(readOnly = true)
    public Optional<SystemPrincipal> resolvePrincipal(String accessToken) {
        if (accessToken == null) {
            return Optional.empty();
        }
        String accessTokenHash = hash(accessToken);
        Optional<SystemPrincipal> cachedPrincipal = authSessionCache.get(accessTokenHash);
        if (cachedPrincipal.isPresent()) {
            return cachedPrincipal;
        }
        AuthenticatedSession session = tokenMapper.selectActiveAccessSession(accessTokenHash, LocalDateTime.now());
        if (session == null) {
            return Optional.empty();
        }
        SystemPrincipal principal = toPrincipal(session);
        authSessionCache.put(accessTokenHash, principal, session.accessExpiresTime());
        return Optional.of(principal);
    }

    @Transactional
    public TokenPair refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(CommonResultCode.UNAUTHORIZED);
        }
        AuthenticatedSession session = tokenMapper.selectActiveRefreshSession(hash(refreshToken), LocalDateTime.now());
        if (session == null) {
            throw new BusinessException(CommonResultCode.UNAUTHORIZED, "账号、租户或成员关系已失效");
        }
        TenantIsolation.withoutTenant(() -> revoke(session.tokenId()));
        authSessionCache.evict(session.accessTokenHash());
        return TenantIsolation.withoutTenant(() ->
                createTokenPair(session.userId(), session.tenantId(), session.username()));
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        TenantIsolation.withoutTenant(() -> {
            if (accessToken != null) {
                String accessTokenHash = hash(accessToken);
                SystemAuthToken token = tokenMapper.selectOne(Wrappers.<SystemAuthToken>lambdaQuery()
                        .eq(SystemAuthToken::getAccessTokenHash, accessTokenHash)
                        .isNull(SystemAuthToken::getRevokedTime)
                        .last("LIMIT 1"));
                if (token != null) {
                    revoke(token.getId());
                    authSessionCache.evict(accessTokenHash);
                    return;
                }
            }
            if (refreshToken != null) {
                SystemAuthToken token = tokenMapper.selectOne(Wrappers.<SystemAuthToken>lambdaQuery()
                        .eq(SystemAuthToken::getRefreshTokenHash, hash(refreshToken))
                        .isNull(SystemAuthToken::getRevokedTime)
                        .last("LIMIT 1"));
                if (token != null) {
                    revoke(token.getId());
                    authSessionCache.evict(token.getAccessTokenHash());
                }
            }
        });
    }

    /**
     * 用户被禁用或移出租户后立即撤销该范围内全部会话，避免等待缓存 TTL 后才生效。
     */
    @Transactional
    public void revokeUserSessions(Long tenantId, Long userId) {
        TenantIsolation.withoutTenant(() -> {
            List<SystemAuthToken> tokens = tokenMapper.selectList(Wrappers.<SystemAuthToken>lambdaQuery()
                    .eq(SystemAuthToken::getTenantId, tenantId)
                    .eq(SystemAuthToken::getUserId, userId)
                    .isNull(SystemAuthToken::getRevokedTime));
            revokeSessions(tokens);
        });
    }

    @Transactional
    public void revokeAllUserSessions(Long userId) {
        TenantIsolation.withoutTenant(() -> revokeSessions(tokenMapper.selectList(
                Wrappers.<SystemAuthToken>lambdaQuery()
                        .eq(SystemAuthToken::getUserId, userId)
                        .isNull(SystemAuthToken::getRevokedTime))));
    }

    @Transactional
    public void deleteTenantUserSessions(Long tenantId, Long userId) {
        TenantIsolation.withoutTenant(() -> {
            List<SystemAuthToken> tokens = tokenMapper.selectList(Wrappers.<SystemAuthToken>lambdaQuery()
                    .eq(SystemAuthToken::getTenantId, tenantId)
                    .eq(SystemAuthToken::getUserId, userId));
            authSessionCache.evictAll(tokens.stream().map(SystemAuthToken::getAccessTokenHash).toList());
            tokenMapper.physicallyDeleteUserSessions(tenantId, userId);
        });
    }

    private void revokeSessions(List<SystemAuthToken> tokens) {
        if (tokens.isEmpty()) {
            return;
        }
        LocalDateTime revokedTime = LocalDateTime.now();
        for (SystemAuthToken token : tokens) {
            SystemAuthToken update = new SystemAuthToken();
            update.setId(token.getId());
            update.setRevokedTime(revokedTime);
            update.setUpdateBy(auditOperatorId());
            tokenMapper.updateById(update);
        }
        authSessionCache.evictAll(tokens.stream().map(SystemAuthToken::getAccessTokenHash).toList());
    }

    private Long selectLoginTenant(Long userId, Long requestedTenantId) {
        List<SystemUserTenant> memberships = userTenantMapper.selectList(
                Wrappers.<SystemUserTenant>lambdaQuery()
                        .eq(SystemUserTenant::getUserId, userId)
                        .eq(SystemUserTenant::getStatus, Constants.YES));
        if (memberships.isEmpty()) {
            throw new BusinessException(CommonResultCode.UNAUTHORIZED, "账号未加入有效租户");
        }

        List<Long> memberTenantIds = memberships.stream().map(SystemUserTenant::getTenantId).distinct().toList();
        if (requestedTenantId != null) {
            if (!memberTenantIds.contains(requestedTenantId)) {
                throw new BusinessException(CommonResultCode.UNAUTHORIZED, "无权访问指定租户");
            }
        }
        LocalDateTime now = LocalDateTime.now();
        Map<Long, SystemTenant> activeTenants = tenantMapper.selectList(Wrappers.<SystemTenant>lambdaQuery()
                        .in(SystemTenant::getId, memberTenantIds)
                        .eq(SystemTenant::getStatus, Constants.YES)
                        .and(query -> query.isNull(SystemTenant::getExpireTime)
                                .or().gt(SystemTenant::getExpireTime, now)))
                .stream()
                .collect(Collectors.toMap(SystemTenant::getId, Function.identity()));

        if (requestedTenantId != null) {
            if (!activeTenants.containsKey(requestedTenantId)) {
                throw new BusinessException(CommonResultCode.UNAUTHORIZED, "无权访问指定租户");
            }
            return requestedTenantId;
        }
        if (activeTenants.size() == 1) {
            return activeTenants.keySet().iterator().next();
        }
        if (activeTenants.isEmpty()) {
            throw new BusinessException(CommonResultCode.UNAUTHORIZED, "账号未加入有效租户");
        }
        throw new BusinessException(CommonResultCode.CONFLICT, "账号属于多个租户，请选择租户后登录");
    }

    private TokenPair createTokenPair(Long userId, Long tenantId, String username) {
        LocalDateTime now = LocalDateTime.now();
        String accessToken = randomToken();
        String refreshToken = randomToken();
        SystemAuthToken entity = new SystemAuthToken();
        entity.setTenantId(tenantId);
        entity.setUserId(userId);
        entity.setAccessTokenHash(hash(accessToken));
        entity.setRefreshTokenHash(hash(refreshToken));
        entity.setAccessExpiresTime(now.plus(authProperties.getAccessTokenTtl()));
        entity.setRefreshExpiresTime(now.plus(authProperties.getRefreshTokenTtl()));
        entity.setCreateBy(Constants.SYSTEM_USER_ID);
        entity.setUpdateBy(Constants.SYSTEM_USER_ID);
        tokenMapper.insert(entity);
        cacheAfterCommit(entity.getAccessTokenHash(), new SystemPrincipal(userId, tenantId, username),
                entity.getAccessExpiresTime());
        return new TokenPair(accessToken, refreshToken, entity.getAccessExpiresTime(), entity.getRefreshExpiresTime());
    }

    private void revoke(Long tokenId) {
        SystemAuthToken token = new SystemAuthToken();
        token.setId(tokenId);
        token.setRevokedTime(LocalDateTime.now());
        // access token 过期但 refresh token 仍有效时，SecurityContext 不存在，使用系统审计操作者兜底。
        token.setUpdateBy(auditOperatorId());
        tokenMapper.updateById(token);
    }

    private Long auditOperatorId() {
        return Optional.ofNullable(SecurityUtils.getCurrentUserId()).orElse(Constants.SYSTEM_USER_ID);
    }

    private SystemPrincipal toPrincipal(AuthenticatedSession session) {
        return new SystemPrincipal(session.userId(), session.tenantId(), session.username());
    }

    private void cacheAfterCommit(String accessTokenHash, SystemPrincipal principal,
                                  LocalDateTime accessExpiresTime) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            authSessionCache.put(accessTokenHash, principal, accessExpiresTime);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                authSessionCache.put(accessTokenHash, principal, accessExpiresTime);
            }
        });
    }

    private String randomToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    public record TokenPair(String accessToken, String refreshToken,
                            LocalDateTime accessExpiresTime, LocalDateTime refreshExpiresTime) {
    }
}
