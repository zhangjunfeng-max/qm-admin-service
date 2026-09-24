package com.qm.admin.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "字典项")
public class DictItemResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dictId;
    private String itemName;
    private String itemCode;
    private Integer sort;
    private String color;
    private String icon;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
