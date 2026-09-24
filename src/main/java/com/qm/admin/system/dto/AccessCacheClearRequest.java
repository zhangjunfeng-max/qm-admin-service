package com.qm.admin.system.dto;

import jakarta.validation.constraints.Pattern;

public record AccessCacheClearRequest(
        @Pattern(regexp = "CURRENT_USER|TENANT", message = "scope 只能是 CURRENT_USER 或 TENANT")
        String scope
) {
    public String normalizedScope() { return scope == null || scope.isBlank() ? "CURRENT_USER" : scope; }
}
