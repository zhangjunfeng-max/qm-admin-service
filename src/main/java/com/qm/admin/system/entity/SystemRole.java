package com.qm.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.qm.admin.common.model.BaseIdEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_role")
public class SystemRole extends BaseIdEntity {

    private Long tenantId;
    private String roleCode;
    private String roleName;
    private Integer status;
    private String remark;
}
