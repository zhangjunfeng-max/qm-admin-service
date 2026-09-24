package com.qm.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.qm.admin.common.model.BaseIdEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("sys_tenant")
public class SystemTenant extends BaseIdEntity {

    private String tenantCode;
    private String tenantName;
    private String tenantType;
    private String contactName;
    private String contactPhone;
    private Integer status;
    private LocalDateTime expireTime;
    private String remark;
}
