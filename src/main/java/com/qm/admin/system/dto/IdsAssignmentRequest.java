package com.qm.admin.system.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record IdsAssignmentRequest(@NotNull List<Long> ids) {}
