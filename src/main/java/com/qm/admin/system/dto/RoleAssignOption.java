package com.qm.admin.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record RoleAssignOption(@JsonSerialize(using = ToStringSerializer.class) Long id,
                               String roleCode, String roleName, Integer status) {}
