package com.qm.admin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "编辑系统用户请求")
public record UserUpdateRequest(
        @NotBlank(message = "姓名不能为空")
        @Size(max = 64, message = "姓名长度不能超过 64")
        String realName,
        @Size(max = 512, message = "头像地址长度不能超过 512")
        String avatar,
        @Size(max = 255, message = "用户描述长度不能超过 255")
        String description,
        @Min(value = 0, message = "账号状态不合法") @Max(value = 1, message = "账号状态不合法")
        Integer accountStatus,
        @Min(value = 0, message = "成员状态不合法") @Max(value = 1, message = "成员状态不合法")
        Integer memberStatus,
        @Min(value = 0, message = "租户管理员标识不合法") @Max(value = 1, message = "租户管理员标识不合法")
        Integer isTenantAdmin
) {
}
