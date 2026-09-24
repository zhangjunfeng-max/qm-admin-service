package com.qm.admin.system.security;

/**
 * 认证上下文只保存后续查询必需的稳定标识，避免把密码、令牌等敏感数据放入上下文。
 */
public record SystemPrincipal(Long userId, Long tenantId, String username) {
}
