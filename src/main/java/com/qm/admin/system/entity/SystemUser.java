package com.qm.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.qm.admin.common.model.BaseIdEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_user")
public class SystemUser extends BaseIdEntity {

    private String username;
    private String passwordHash;
    private String realName;
    private String avatar;
    private String description;
    private String homePath;
    private Integer status;
}
