package com.qm.admin.system.security;

import java.util.function.Supplier;

/**
 * 仅供认证链路在尚未建立 SecurityContext 时读取租户成员和令牌数据。
 *
 * <p>普通业务代码不得使用该绕过能力，否则会失去 MyBatis-Plus 的自动租户隔离保护。</p>
 */
public final class TenantIsolation {

    private static final ThreadLocal<Integer> IGNORE_DEPTH = ThreadLocal.withInitial(() -> 0);

    private TenantIsolation() {
    }

    public static boolean isIgnored() {
        return IGNORE_DEPTH.get() > 0;
    }

    public static <T> T withoutTenant(Supplier<T> action) {
        IGNORE_DEPTH.set(IGNORE_DEPTH.get() + 1);
        try {
            return action.get();
        } finally {
            int depth = IGNORE_DEPTH.get() - 1;
            if (depth == 0) {
                IGNORE_DEPTH.remove();
            } else {
                IGNORE_DEPTH.set(depth);
            }
        }
    }

    public static void withoutTenant(Runnable action) {
        withoutTenant(() -> {
            action.run();
            return null;
        });
    }
}
