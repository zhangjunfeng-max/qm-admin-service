package com.qm.admin.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
@Schema(description = "系统租户列表项")
public class TenantPageItemResponse {
    @JsonSerialize(using = ToStringSerializer.class) private Long id;
    private String tenantCode;
    private String tenantName;
    private String tenantType;
    private String contactName;
    private String contactPhone;
    private Integer status;
    private LocalDateTime expireTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
