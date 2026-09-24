package com.qm.admin.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "系统菜单树节点")
public class MenuPageItemResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
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
    private Boolean hasChildren;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
