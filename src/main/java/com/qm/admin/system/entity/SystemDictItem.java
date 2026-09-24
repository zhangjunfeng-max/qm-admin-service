package com.qm.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.qm.admin.common.model.BaseIdEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_dict_item")
public class SystemDictItem extends BaseIdEntity {

    private Long tenantId;
    private Long dictId;
    private String itemName;
    private String itemCode;
    private Integer sort;
    private String color;
    private String icon;
    private Integer status;
}
