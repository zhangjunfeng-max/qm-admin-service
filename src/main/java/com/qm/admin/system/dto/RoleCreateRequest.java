package com.qm.admin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "新增系统角色请求")
public record RoleCreateRequest(
        @NotBlank(message = "角色编码不能为空")
        @Size(max = 64, message = "角色编码长度不能超过 64")
        @Pattern(regexp = "^[A-Za-z0-9_.:-]+$", message = "角色编码只能包含字母、数字、下划线、点、冒号和短横线")
        String roleCode,
        @NotBlank(message = "角色名称不能为空")
        @Size(max = 64, message = "角色名称长度不能超过 64")
        String roleName,
        @Min(value = 0, message = "角色状态不合法") @Max(value = 1, message = "角色状态不合法")
        Integer status,
        @Size(max = 500, message = "角色备注长度不能超过 500")
        String remark
) {
}
