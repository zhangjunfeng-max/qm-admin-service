package com.qm.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.qm.admin.common.model.BaseIdEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_menu")
public class SystemMenu extends BaseIdEntity {

    private Long parentId;
    private String menuType;
    private String name;
    private String path;
    private String component;
    private String redirect;
    private String icon;
    private String title;
    private Integer sort;
    private Integer status;
    private Integer visible;
    private Integer keepAlive;
    private Integer affixTab;
    private String authCode;
}
