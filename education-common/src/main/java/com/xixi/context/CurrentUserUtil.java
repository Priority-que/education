package com.xixi.context;

import com.xixi.exception.BizException;

/**
 * 当前登录用户工具类。
 * 业务层可以直接调用本类获取 userId / role，避免重复写判空逻辑。
 */
public final class CurrentUserUtil {
    private static final int ADMIN_ROLE = 1;

    private CurrentUserUtil() {
    }

    /**
     * 获取当前用户ID（可能为 null）。
     */
    public static Long getUserId() {
        return CurrentUserContext.getUserId();
    }

    /**
     * 获取当前用户ID，不存在则抛出 401。
     */
    public static Long requireUserId() {
        Long userId = CurrentUserContext.getUserId();
        if (userId == null) {
            throw new BizException(401, "未登录或用户上下文不存在");
        }
        return userId;
    }

    /**
     * 获取当前用户角色（可能为 null）。
     */
    public static Integer getRole() {
        return CurrentUserContext.getRole();
    }

    /**
     * 获取当前用户角色，不存在则抛出 401。
     */
    public static Integer requireRole() {
        Integer role = CurrentUserContext.getRole();
        if (role == null) {
            throw new BizException(401, "未登录或用户角色缺失");
        }
        return role;
    }

    /**
     * 是否管理员角色（role=1）。
     */
    public static boolean isAdmin() {
        Integer role = CurrentUserContext.getRole();
        return role != null && role == ADMIN_ROLE;
    }
}

