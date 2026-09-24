package com.qm.admin.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "通用 Long 主键实体")
public abstract class BaseIdEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键 ID")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
