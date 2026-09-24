package com.qm.admin.system.dto;

import java.time.LocalDateTime;

/**
 * 认证查询结果。一次联表查询同时验证令牌、用户、租户成员关系和租户有效状态。
 */
public record AuthenticatedSession(
        Long tokenId,
        Long tenantId,
        Long userId,
        String username,
        String accessTokenHash,
        String refreshTokenHash,
        LocalDateTime accessExpiresTime,
        LocalDateTime refreshExpiresTime
) {
}
