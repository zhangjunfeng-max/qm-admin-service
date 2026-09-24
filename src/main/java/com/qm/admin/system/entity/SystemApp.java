package com.qm.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.qm.admin.common.model.BaseIdEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_app")
public class SystemApp extends BaseIdEntity {

    private Long tenantId;
    private String appid;
    private String appCode;
    private String appName;
    private String appType;
    private Integer status;
    private String remark;
}
