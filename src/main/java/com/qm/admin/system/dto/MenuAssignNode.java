package com.qm.admin.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.List;

public record MenuAssignNode(@JsonSerialize(using = ToStringSerializer.class) Long id,
                             String title, String menuType,
                             String authCode,
                             List<MenuAssignNode> children) {}
