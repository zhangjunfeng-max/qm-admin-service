package com.qm.admin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "编辑系统租户请求，租户编码不可修改")
public record TenantUpdateRequest(
        @NotBlank @Size(max = 128) String tenantName,
        @Pattern(regexp = "SINGLE|CHAIN") String tenantType,
        @Size(max = 64) String contactName,
        @Size(max = 32) String contactPhone,
        @Min(0) @Max(1) Integer status,
        LocalDateTime expireTime,
        @Size(max = 500) String remark
) {}
