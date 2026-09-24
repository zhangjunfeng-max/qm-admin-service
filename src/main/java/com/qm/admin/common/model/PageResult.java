package com.qm.admin.common.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "分页响应数据")
public record PageResult<T>(
        @Schema(description = "当前页数据")
        List<T> records,
        @Schema(description = "总条数", example = "100")
        long total,
        @Schema(description = "页码，从 1 开始", example = "1")
        long pageNum,
        @Schema(description = "每页条数", example = "10")
        long pageSize
) {

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
}
