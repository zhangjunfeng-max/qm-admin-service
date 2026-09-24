package com.qm.admin.system.controller;

import com.qm.admin.common.annotation.AnonymousAccess;
import com.qm.admin.system.config.SystemAuthProperties;
import com.qm.admin.system.dto.LoginRequest;
import com.qm.admin.system.dto.LoginResponse;
import com.qm.admin.system.security.SystemPrincipal;
import com.qm.admin.system.security.TenantHeaders;
import com.qm.admin.system.security.TokenAuthenticationFilter;
import com.qm.admin.system.service.SystemAccessService;
import com.qm.admin.system.service.SystemAuthService;
import com.qm.admin.system.service.SystemAccessCache;
import com.qm.admin.system.dto.AccessCacheClearRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Tag(name = "SystemAuth", description = "系统认证")
@RestController
@RequestMapping("/system/auth")
public class AuthController {

    private final SystemAuthService authService;
    private final SystemAccessService accessService;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final SystemAuthProperties authProperties;
    private final SystemAccessCache accessCache;

    public AuthController(SystemAuthService authService, SystemAccessService accessService,
                          TokenAuthenticationFilter tokenAuthenticationFilter,
                          SystemAuthProperties authProperties, SystemAccessCache accessCache) {
        this.authService = authService;
        this.accessService = accessService;
        this.tokenAuthenticationFilter = tokenAuthenticationFilter;
        this.authProperties = authProperties;
        this.accessCache = accessCache;
    }

    @AnonymousAccess
    @Operation(summary = "登录")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request,
                               @RequestHeader(name = TenantHeaders.TENANT_ID, required = false) Long tenantId,
                               HttpServletResponse response) {
        SystemAuthService.TokenPair tokenPair = authService.login(request.username(), request.password(), tenantId);
        writeRefreshCookie(response, tokenPair.refreshToken(), authProperties.getRefreshTokenTtl());
        return new LoginResponse(tokenPair.accessToken());
    }

    @AnonymousAccess
    @Operation(summary = "刷新访问令牌")
    @PostMapping("/refresh")
    public String refresh(HttpServletRequest request, HttpServletResponse response) {
        SystemAuthService.TokenPair tokenPair = authService.refresh(resolveRefreshToken(request));
        writeRefreshCookie(response, tokenPair.refreshToken(), authProperties.getRefreshTokenTtl());
        return tokenPair.accessToken();
    }

    @AnonymousAccess
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(tokenAuthenticationFilter.resolveToken(request), resolveRefreshToken(request));
        writeRefreshCookie(response, "", Duration.ZERO);
    }

    @Operation(summary = "获取当前用户权限码")
    @GetMapping("/codes")
    public List<String> getAccessCodes(@AuthenticationPrincipal SystemPrincipal principal) {
        return accessService.getPermissions(principal);
    }

    @Operation(summary = "清理鉴权业务缓存")
    @PreAuthorize("@ss.hasPermission('system:cache:clear')")
    @DeleteMapping("/cache")
    public void clearCache(@Valid @RequestBody(required = false) AccessCacheClearRequest request,
                           @AuthenticationPrincipal SystemPrincipal principal) {
        String scope = request == null ? "CURRENT_USER" : request.normalizedScope();
        if ("TENANT".equals(scope)) {
            accessCache.evictTenant(principal.tenantId());
        } else {
            accessCache.evictUser(principal.tenantId(), principal.userId());
        }
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> authProperties.getRefreshCookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void writeRefreshCookie(HttpServletResponse response, String token, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(authProperties.getRefreshCookieName(), token)
                .httpOnly(true)
                .secure(authProperties.isSecureCookie())
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
