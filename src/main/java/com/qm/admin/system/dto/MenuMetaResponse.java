package com.qm.admin.system.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MenuMetaResponse(
        String title,
        String icon,
        Integer order,
        Boolean keepAlive,
        Boolean affixTab
) {
}
