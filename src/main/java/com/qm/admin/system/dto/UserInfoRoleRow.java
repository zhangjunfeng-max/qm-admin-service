package com.qm.admin.system.dto;

/**
 * 用户基础信息和当前租户角色的扁平查询结果，由服务层合并为用户响应。
 */
public record UserInfoRoleRow(
        Long userId,
        String username,
        String realName,
        String avatar,
        String description,
        String homePath,
        String roleCode
) {
}
