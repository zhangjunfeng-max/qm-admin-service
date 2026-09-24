package com.qm.admin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "批量删除当前租户角色请求")
public record RoleBatchDeleteRequest(
        @NotEmpty(message = "请选择要删除的角色")
        @Size(max = 200, message = "单次最多删除 200 个角色")
        List<Long> roleIds
) {
}
