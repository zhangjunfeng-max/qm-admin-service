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

@Schema(description = "系统角色分页查询参数")
public class RolePageQuery extends PageQuery {

    @Size(max = 100)
    @Schema(description = "关键词，匹配角色编码、名称或备注")
    private String keyword;

    @Min(0)
    @Max(1)
    @Schema(description = "角色状态：0 禁用、1 启用")
    private Integer status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "创建时间起点")
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "创建时间终点")
    private LocalDateTime endTime;

    @Pattern(regexp = "id|roleCode|roleName|status|createTime|updateTime")
    @Schema(description = "排序字段")
    private String sortBy = "createTime";

    @Pattern(regexp = "asc|desc")
    @Schema(description = "排序方向：asc 或 desc")
    private String sortOrder = "desc";

    public RolePageQuery() {
        setPageSize(20);
    }

    @AssertTrue(message = "pageSize 仅支持 20、50、100、200")
    public boolean isSupportedPageSize() {
        return PageQueryUtils.isSupportedPageSize(getPageSize());
    }

    @AssertTrue(message = "创建时间起点不能晚于终点")
    public boolean isTimeRangeValid() {
        return PageQueryUtils.isValidTimeRange(startTime, endTime);
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
