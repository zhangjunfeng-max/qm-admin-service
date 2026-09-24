package com.qm.admin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "编辑字典项请求")
public record DictItemUpdateRequest(
        @NotBlank @Size(max = 64) String itemName,
        @NotBlank @Size(max = 64) String itemCode,
        @Min(0) @Max(999999) Integer sort,
        @Size(max = 32) String color,
        @Size(max = 128) String icon,
        @Min(0) @Max(1) Integer status
) {}
