package com.qm.admin.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.List;

public record UserRoleAssignmentResponse(List<RoleAssignOption> roles,
                                         @JsonSerialize(contentUsing = ToStringSerializer.class)
                                         List<Long> selectedRoleIds) {}
