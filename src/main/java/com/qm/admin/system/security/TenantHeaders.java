package com.qm.admin.system.security;

/**
 * 租户请求头只用于登录时选择租户；认证完成后，实际租户身份始终以令牌记录为准。
 */
public final class TenantHeaders {

    public static final String TENANT_ID = "tenant-id";

    private TenantHeaders() {
    }
}
