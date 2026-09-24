package com.qm.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.qm.admin.common.model.BaseIdEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_dict")
public class SystemDict extends BaseIdEntity {

    private Long tenantId;
    private String dictName;
    private String dictCode;
    private Integer status;
    private String remark;
}
