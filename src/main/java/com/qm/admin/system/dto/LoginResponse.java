package com.qm.admin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录响应")
public record LoginResponse(String accessToken) {
}
