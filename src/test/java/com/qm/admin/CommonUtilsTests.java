package com.qm.admin;

import com.qm.admin.common.util.PageQueryUtils;
import com.qm.admin.common.util.ValueUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonUtilsTests {

    @Test
    void defaultsOnlyNullValues() {
        assertEquals("fallback", ValueUtils.defaultIfNull(null, "fallback"));
        assertEquals("value", ValueUtils.defaultIfNull("value", "fallback"));
        assertEquals("", ValueUtils.defaultString(null));
        assertEquals(" ", ValueUtils.defaultString(" "));
    }

    @Test
    void validatesSharedPageQueryRules() {
        assertTrue(PageQueryUtils.isSupportedPageSize(20));
        assertFalse(PageQueryUtils.isSupportedPageSize(10));

        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = start.plusDays(1);
        assertTrue(PageQueryUtils.isValidTimeRange(start, end));
        assertTrue(PageQueryUtils.isValidTimeRange(null, end));
        assertFalse(PageQueryUtils.isValidTimeRange(end, start));
    }
}
