package com.qm.admin.system.util;

import com.qm.admin.system.security.SystemPrincipal;
import jakarta.annotation.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * @description:
 * @author: ZhangJunFeng
 * @date: 2026/9/19 16:45
 */
public class SecurityUtils {


    /**
     * 获取系统用户
     * @return
     */
    public static Optional<SystemPrincipal> getSystemPrincipalOptions() {
        Authentication authentication = getSecurityContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SystemPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    /**
     * 获取系统用户信息
     * @return
     */
    public static @Nullable SystemPrincipal getSystemPrincipal() {
        return getSystemPrincipalOptions().orElse(null);
    }

    public static @Nullable SystemPrincipal getCurrentPrincipal() {
        return getSystemPrincipal();
    }

    /**
     * 获取当前用户id
     * @return
     */
    public static Long getCurrentUserId() {
        return getSystemPrincipalOptions().map(SystemPrincipal::userId).orElse(null);
    }

    /**
     * 获取当前认证会话绑定的租户 ID。租户身份来自服务端令牌，不从普通业务参数获取。
     */
    public static Long getCurrentTenantId() {
        return getSystemPrincipalOptions().map(SystemPrincipal::tenantId).orElse(null);
    }

    public static SecurityContext getSecurityContext() {
        return SecurityContextHolder.getContext();
    }
}
