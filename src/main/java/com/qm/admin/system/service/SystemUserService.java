package com.qm.admin.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qm.admin.common.constants.Constants;
import com.qm.admin.common.exception.BusinessException;
import com.qm.admin.common.model.PageResult;
import com.qm.admin.common.response.CommonResultCode;
import com.qm.admin.system.dto.UserCreateRequest;
import com.qm.admin.system.dto.UserInfoResponse;
import com.qm.admin.system.dto.UserInfoRoleRow;
import com.qm.admin.system.dto.UserPageItemResponse;
import com.qm.admin.system.dto.UserPageQuery;
import com.qm.admin.system.dto.UserUpdateRequest;
import com.qm.admin.system.dto.IdsAssignmentRequest;
import com.qm.admin.system.dto.RoleAssignOption;
import com.qm.admin.system.dto.UserRoleAssignmentResponse;
import com.qm.admin.system.entity.SystemUser;
import com.qm.admin.system.entity.SystemUserRole;
import com.qm.admin.system.entity.SystemUserTenant;
import com.qm.admin.system.mapper.SystemUserRoleMapper;
import com.qm.admin.system.mapper.SystemUserTenantMapper;
import com.qm.admin.system.mapper.SystemUserMapper;
import com.qm.admin.system.mapper.SystemRoleMapper;
import com.qm.admin.system.security.SystemPrincipal;
import com.qm.admin.system.security.TenantIsolation;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemUserService {

    private static final String DEFAULT_HOME_PATH = "/system/user";
    private static final String LEGACY_DASHBOARD_PATH_PREFIX = "/dashboard";

    private final SystemUserMapper userMapper;
    private final SystemUserTenantMapper userTenantMapper;
    private final SystemUserRoleMapper userRoleMapper;
    private final SystemRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final SystemAuthService authService;
    private final SystemAccessCache accessCache;
    private final SystemAccessCacheInvalidator cacheInvalidator;

    public SystemUserService(SystemUserMapper userMapper, SystemUserTenantMapper userTenantMapper,
                             SystemUserRoleMapper userRoleMapper, SystemRoleMapper roleMapper,
                             PasswordEncoder passwordEncoder,
                             SystemAuthService authService, SystemAccessCache accessCache,
                             SystemAccessCacheInvalidator cacheInvalidator) {
        this.userMapper = userMapper;
        this.userTenantMapper = userTenantMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.accessCache = accessCache;
        this.cacheInvalidator = cacheInvalidator;
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(SystemPrincipal principal) {
        return accessCache.getOrLoadUserInfo(principal.tenantId(), principal.userId(), () -> {
            List<UserInfoRoleRow> rows = userMapper.selectUserInfoWithRoles(principal.userId(), principal.tenantId());
            if (rows.isEmpty()) {
                throw new BusinessException(CommonResultCode.UNAUTHORIZED);
            }
            UserInfoRoleRow user = rows.getFirst();
            return new UserInfoResponse(
                    String.valueOf(user.userId()), user.username(), user.realName(), user.avatar(),
                    user.description(), normalizeHomePath(user.homePath()),
                    rows.stream().map(UserInfoRoleRow::roleCode)
                            .filter(java.util.Objects::nonNull).distinct().toList()
            );
        });
    }

    @Transactional(readOnly = true)
    public PageResult<UserPageItemResponse> getUserPage(UserPageQuery query, SystemPrincipal principal) {
        Page<UserPageItemResponse> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(userMapper.selectUserPage(page, principal.tenantId(), query));
    }

    @Transactional(readOnly = true)
    public UserPageItemResponse getUser(Long userId, SystemPrincipal principal) {
        UserPageItemResponse user = userMapper.selectTenantUser(principal.tenantId(), userId);
        if (user == null) {
            throw new BusinessException(CommonResultCode.NOT_FOUND, "当前租户下不存在该用户");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public UserRoleAssignmentResponse getRoleAssignment(Long userId, SystemPrincipal principal) {
        getUser(userId, principal);
        List<RoleAssignOption> roles = roleMapper.selectTenantRoleOptions(principal.tenantId());
        List<Long> selected = userRoleMapper.selectList(Wrappers.<SystemUserRole>lambdaQuery()
                        .eq(SystemUserRole::getTenantId, principal.tenantId())
                        .eq(SystemUserRole::getUserId, userId))
                .stream().map(SystemUserRole::getRoleId).toList();
        return new UserRoleAssignmentResponse(roles, selected);
    }

    @Transactional
    public void assignRoles(Long userId, IdsAssignmentRequest request, SystemPrincipal principal) {
        getUser(userId, principal);
        List<Long> roleIds = request.ids().stream().distinct().toList();
        if (!roleIds.isEmpty() && roleMapper.countTenantRoles(principal.tenantId(), roleIds) != roleIds.size()) {
            throw new BusinessException(CommonResultCode.NOT_FOUND, "存在不属于当前租户的角色");
        }
        userRoleMapper.delete(Wrappers.<SystemUserRole>lambdaQuery()
                .eq(SystemUserRole::getTenantId, principal.tenantId())
                .eq(SystemUserRole::getUserId, userId));
        roleIds.forEach(roleId -> {
            SystemUserRole relation = new SystemUserRole();
            relation.setTenantId(principal.tenantId());
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            relation.setCreateBy(principal.userId());
            userRoleMapper.insert(relation);
        });
        cacheInvalidator.user(principal.tenantId(), userId);
    }

    @Transactional
    public UserPageItemResponse createUser(UserCreateRequest request, SystemPrincipal principal) {
        SystemUser existing = userMapper.selectOne(Wrappers.<SystemUser>lambdaQuery()
                .eq(SystemUser::getUsername, request.username())
                .last("LIMIT 1"));
        if (existing != null) {
            throw new BusinessException(CommonResultCode.CONFLICT, "用户名已存在");
        }

        SystemUser user = new SystemUser();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRealName(request.realName());
        user.setAvatar(defaultString(request.avatar()));
        user.setDescription(defaultString(request.description()));
        user.setHomePath(DEFAULT_HOME_PATH);
        user.setStatus(defaultValue(request.accountStatus(), Constants.YES));
        userMapper.insert(user);

        SystemUserTenant membership = new SystemUserTenant();
        membership.setTenantId(principal.tenantId());
        membership.setUserId(user.getId());
        membership.setIsTenantAdmin(defaultValue(request.isTenantAdmin(), Constants.NO));
        membership.setStatus(defaultValue(request.memberStatus(), Constants.YES));
        membership.setCreateBy(principal.userId());
        membership.setUpdateBy(principal.userId());
        userTenantMapper.insert(membership);
        return getUser(user.getId(), principal);
    }

    @Transactional
    public UserPageItemResponse updateUser(Long userId, UserUpdateRequest request, SystemPrincipal principal) {
        UserPageItemResponse current = getUser(userId, principal);
        int accountStatus = defaultValue(request.accountStatus(), current.getAccountStatus());
        int memberStatus = defaultValue(request.memberStatus(), current.getMemberStatus());
        if (userId.equals(principal.userId())
                && (accountStatus == Constants.NO || memberStatus == Constants.NO)) {
            throw new BusinessException(CommonResultCode.CONFLICT, "不能禁用当前登录用户");
        }

        SystemUser user = new SystemUser();
        user.setId(userId);
        user.setRealName(request.realName());
        user.setAvatar(defaultString(request.avatar()));
        user.setDescription(defaultString(request.description()));
        user.setStatus(accountStatus);
        userMapper.updateById(user);

        SystemUserTenant membership = selectMembership(principal.tenantId(), userId);
        membership.setStatus(memberStatus);
        membership.setIsTenantAdmin(defaultValue(request.isTenantAdmin(), current.getIsTenantAdmin()));
        membership.setUpdateBy(principal.userId());
        userTenantMapper.updateById(membership);

        if (accountStatus == Constants.NO) {
            authService.revokeAllUserSessions(userId);
        } else if (memberStatus == Constants.NO) {
            authService.revokeUserSessions(principal.tenantId(), userId);
        }
        // sys_user 是跨租户账号资料，修改账号状态或资料必须使所有租户视图同时失效。
        cacheInvalidator.all();
        return getUser(userId, principal);
    }

    @Transactional
    public void deleteUsers(List<Long> userIds, SystemPrincipal principal) {
        List<Long> distinctIds = userIds.stream().distinct().toList();
        if (distinctIds.contains(principal.userId())) {
            throw new BusinessException(CommonResultCode.CONFLICT, "不能删除当前登录用户");
        }

        List<SystemUserTenant> memberships = userTenantMapper.selectList(
                Wrappers.<SystemUserTenant>lambdaQuery()
                        .eq(SystemUserTenant::getTenantId, principal.tenantId())
                        .in(SystemUserTenant::getUserId, distinctIds));
        if (memberships.size() != distinctIds.size()) {
            throw new BusinessException(CommonResultCode.NOT_FOUND, "部分用户不属于当前租户");
        }

        userRoleMapper.delete(Wrappers.<SystemUserRole>lambdaQuery()
                .eq(SystemUserRole::getTenantId, principal.tenantId())
                .in(SystemUserRole::getUserId, distinctIds));
        for (Long userId : distinctIds) {
            authService.deleteTenantUserSessions(principal.tenantId(), userId);
        }
        userTenantMapper.deleteBatchIds(memberships.stream().map(SystemUserTenant::getId).toList());

        for (Long userId : distinctIds) {
            Long membershipCount = TenantIsolation.withoutTenant(() -> userTenantMapper.selectCount(
                    Wrappers.<SystemUserTenant>lambdaQuery().eq(SystemUserTenant::getUserId, userId)));
            if (membershipCount == 0) {
                userMapper.deleteById(userId);
            }
            cacheInvalidator.user(principal.tenantId(), userId);
        }
    }

    private SystemUserTenant selectMembership(Long tenantId, Long userId) {
        SystemUserTenant membership = userTenantMapper.selectOne(Wrappers.<SystemUserTenant>lambdaQuery()
                .eq(SystemUserTenant::getTenantId, tenantId)
                .eq(SystemUserTenant::getUserId, userId)
                .last("LIMIT 1"));
        if (membership == null) {
            throw new BusinessException(CommonResultCode.NOT_FOUND, "当前租户下不存在该用户");
        }
        return membership;
    }

    /**
     * 管理端已移除示例 Dashboard 路由。兼容升级前保存的首页配置，避免登录后落入 404。
     */
    private String normalizeHomePath(String homePath) {
        if (homePath == null || homePath.isBlank() || homePath.startsWith(LEGACY_DASHBOARD_PATH_PREFIX)) {
            return DEFAULT_HOME_PATH;
        }
        return homePath;
    }

    private int defaultValue(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
