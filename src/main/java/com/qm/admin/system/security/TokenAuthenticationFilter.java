package com.qm.admin.system.security;

import com.qm.admin.system.service.SystemAuthService;
import com.qm.admin.system.util.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * @author zjf
 */
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final SystemAuthService systemAuthService;

    public TokenAuthenticationFilter(SystemAuthService systemAuthService) {
        this.systemAuthService = systemAuthService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (SecurityUtils.getSecurityContext().getAuthentication() == null && token != null) {
            systemAuthService.resolvePrincipal(token).ifPresent(principal -> {
                Long requestedTenantId = resolveRequestedTenantId(request);
                if (requestedTenantId != null && !requestedTenantId.equals(principal.tenantId())) {
                    return;
                }
                var authentication = UsernamePasswordAuthenticationToken.authenticated(principal, token, List.of());
                SecurityUtils.getSecurityContext().setAuthentication(authentication);
            });
        }
        filterChain.doFilter(request, response);
    }

    public String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        return StringUtils.hasText(token) ? token : null;
    }

    private Long resolveRequestedTenantId(HttpServletRequest request) {
        String value = request.getHeader(TenantHeaders.TENANT_ID);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return Long.MIN_VALUE;
        }
    }
}
