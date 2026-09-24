package com.qm.admin.system.dto;

import com.qm.admin.common.model.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "字典分页查询参数")
public class DictPageQuery extends PageQuery {

    @Size(max = 100)
    private String keyword;

    @Min(0)
    @Max(1)
    private Integer status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    @Pattern(regexp = "id|dictName|dictCode|status|createTime|updateTime")
    private String sortBy = "createTime";

    @Pattern(regexp = "asc|desc")
    private String sortOrder = "desc";

    public DictPageQuery() {
        setPageSize(20);
    }

    @AssertTrue(message = "pageSize 仅支持 20、50、100、200")
    public boolean isSupportedPageSize() {
        return SUPPORTED_PAGE_SIZES.contains(getPageSize());
    }

    @AssertTrue(message = "创建时间起点不能晚于终点")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || !startTime.isAfter(endTime);
    }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
}
