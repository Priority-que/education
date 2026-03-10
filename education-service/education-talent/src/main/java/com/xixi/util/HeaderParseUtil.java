package com.xixi.util;

import org.springframework.util.StringUtils;

/**
 * 请求头解析工具类。
 */
public final class HeaderParseUtil {

    private HeaderParseUtil() {
    }

    /**
     * 解析用户ID请求头。
     */
    public static Long parseUserId(String header) {
        if (!StringUtils.hasText(header)) {
            return null;
        }
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析角色请求头。
     */
    public static Integer parseRole(String header) {
        if (!StringUtils.hasText(header)) {
            return null;
        }
        try {
            return Integer.parseInt(header.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
