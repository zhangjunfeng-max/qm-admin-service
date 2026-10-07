package com.qm.admin.common.util;

import com.qm.admin.common.model.PageQuery;

import java.time.LocalDateTime;

/**
 * 分页查询参数的公共校验逻辑。
 */
public final class PageQueryUtils {

    private PageQueryUtils() {
    }

    public static boolean isSupportedPageSize(long pageSize) {
        return PageQuery.SUPPORTED_PAGE_SIZES.contains(pageSize);
    }

    public static boolean isValidTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return startTime == null || endTime == null || !startTime.isAfter(endTime);
    }
}
