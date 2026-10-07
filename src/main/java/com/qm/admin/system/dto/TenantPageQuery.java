package com.qm.admin.system.dto;

import com.qm.admin.common.model.PageQuery;
import com.qm.admin.common.util.PageQueryUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "系统租户分页查询参数")
public class TenantPageQuery extends PageQuery {
    @Size(max = 100) private String keyword;
    @Min(0) @Max(1) private Integer status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) private LocalDateTime startTime;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) private LocalDateTime endTime;
    @Pattern(regexp = "id|tenantCode|tenantName|tenantType|status|createTime|updateTime") private String sortBy = "createTime";
    @Pattern(regexp = "asc|desc") private String sortOrder = "desc";
    public TenantPageQuery() { setPageSize(20); }
    @AssertTrue(message = "pageSize 仅支持 20、50、100、200") public boolean isSupportedPageSize() { return PageQueryUtils.isSupportedPageSize(getPageSize()); }
    @AssertTrue(message = "创建时间起点不能晚于终点") public boolean isTimeRangeValid() { return PageQueryUtils.isValidTimeRange(startTime, endTime); }
    public String getKeyword() { return keyword; } public void setKeyword(String value) { keyword = value; }
    public Integer getStatus() { return status; } public void setStatus(Integer value) { status = value; }
    public LocalDateTime getStartTime() { return startTime; } public void setStartTime(LocalDateTime value) { startTime = value; }
    public LocalDateTime getEndTime() { return endTime; } public void setEndTime(LocalDateTime value) { endTime = value; }
    public String getSortBy() { return sortBy; } public void setSortBy(String value) { sortBy = value; }
    public String getSortOrder() { return sortOrder; } public void setSortOrder(String value) { sortOrder = value; }
}
