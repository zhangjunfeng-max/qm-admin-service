package com.qm.admin.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "编辑字典请求")
public record DictUpdateRequest(
        @NotBlank @Size(max = 64) String dictName,
        @Min(0) @Max(1) Integer status,
        @Size(max = 500) String remark
) {}
