package com.qm.admin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "编辑系统角色请求")
public record RoleUpdateRequest(
        @NotBlank(message = "角色名称不能为空")
        @Size(max = 64, message = "角色名称长度不能超过 64")
        String roleName,
        @Min(value = 0, message = "角色状态不合法") @Max(value = 1, message = "角色状态不合法")
        Integer status,
        @Size(max = 500, message = "角色备注长度不能超过 500")
        String remark
) {
}
