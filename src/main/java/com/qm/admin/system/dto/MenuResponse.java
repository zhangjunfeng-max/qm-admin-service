package com.qm.admin.system.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MenuResponse(
        String name,
        String path,
        String component,
        String redirect,
        MenuMetaResponse meta,
        List<MenuResponse> children
) {
}
