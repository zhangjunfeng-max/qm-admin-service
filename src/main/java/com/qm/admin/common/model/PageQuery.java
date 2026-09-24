package com.qm.admin.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

import java.util.Set;

@Getter
@Schema(description = "分页查询参数")
public class PageQuery {

    public static final Set<Long> SUPPORTED_PAGE_SIZES = Set.of(20L, 50L, 100L, 200L);


    @Min(1)
    @Schema(description = "页码，从 1 开始", example = "1")
    private long pageNum = 1;

    @Min(1)
    @Max(200)
    @Schema(description = "每页条数，最大 200", example = "10")
    private long pageSize = 10;

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
