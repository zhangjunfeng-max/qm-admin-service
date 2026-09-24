package com.qm.admin.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "系统用户列表项")
public class UserPageItemResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String username;
    private String realName;
    private String avatar;
    private String description;
    private Integer accountStatus;
    private Integer memberStatus;
    private Integer isTenantAdmin;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
