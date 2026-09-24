package com.qm.admin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "批量移除当前租户用户请求")
public record UserBatchDeleteRequest(
        @NotEmpty(message = "请选择要删除的用户")
        @Size(max = 200, message = "单次最多删除 200 个用户")
        List<Long> userIds
) {
}
