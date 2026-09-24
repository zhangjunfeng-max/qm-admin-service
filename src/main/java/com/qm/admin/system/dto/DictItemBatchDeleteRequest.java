package com.qm.admin.system.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DictItemBatchDeleteRequest(@NotEmpty List<Long> itemIds) {}
