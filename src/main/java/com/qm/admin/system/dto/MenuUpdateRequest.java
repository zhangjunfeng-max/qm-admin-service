package com.qm.admin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "编辑系统菜单请求")
public record MenuUpdateRequest(
        Long parentId,
        @NotBlank @Size(max = 32) String menuType,
        @NotBlank @Size(max = 64) String name,
        @Size(max = 255) String path,
        @Size(max = 255) String component,
        @Size(max = 255) String redirect,
        @Size(max = 255) String icon,
        @NotBlank @Size(max = 128) String title,
        Integer sort,
        @Min(0) @Max(1) Integer status,
        @Min(0) @Max(1) Integer visible,
        @Min(0) @Max(1) Integer keepAlive,
        @Min(0) @Max(1) Integer affixTab,
        @Size(max = 128) String authCode
) {
}
