package com.qm.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.qm.admin.common.model.BaseIdEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("sys_auth_token")
public class SystemAuthToken extends BaseIdEntity {

    private Long tenantId;
    private Long userId;
    private String accessTokenHash;
    private String refreshTokenHash;
    private LocalDateTime accessExpiresTime;
    private LocalDateTime refreshExpiresTime;
    private LocalDateTime revokedTime;
}
