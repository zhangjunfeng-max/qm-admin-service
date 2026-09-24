package com.qm.admin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "当前登录用户信息")
public record UserInfoResponse(
        String userId,
        String username,
        String realName,
        String avatar,
        String desc,
        String homePath,
        List<String> roles
) {
}
