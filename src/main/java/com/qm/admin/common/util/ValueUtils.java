package com.qm.admin.common.util;

/**
 * 常用的空值默认值处理工具。
 *
 * <p>仅封装与业务无关的值替换逻辑，具体业务默认值仍由调用方决定。</p>
 */
public final class ValueUtils {

    private ValueUtils() {
    }

    public static <T> T defaultIfNull(T value, T fallback) {
        return value == null ? fallback : value;
    }

    public static String defaultString(String value) {
        return defaultIfNull(value, "");
    }
}
