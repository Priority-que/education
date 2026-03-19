package com.xixi.context;

/**
 * 当前请求用户上下文（基于 ThreadLocal）。
 *
 * 使用建议：
 * 1. 在拦截器/过滤器中调用 set(userId, role)；
 * 2. 业务代码中按需调用 getUserId()/getRole()；
 * 3. 请求结束后务必调用 clear()，避免线程复用导致脏数据。
 */
public final class CurrentUserContext {
    private static final ThreadLocal<UserPrincipal> CONTEXT = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(Long userId, Integer role) {
        CONTEXT.set(new UserPrincipal(userId, role));
    }

    public static void set(UserPrincipal principal) {
        CONTEXT.set(principal);
    }

    public static UserPrincipal get() {
        return CONTEXT.get();
    }

    public static Long getUserId() {
        UserPrincipal principal = CONTEXT.get();
        return principal == null ? null : principal.getUserId();
    }

    public static Integer getRole() {
        UserPrincipal principal = CONTEXT.get();
        return principal == null ? null : principal.getRole();
    }

    public static boolean hasUser() {
        return CONTEXT.get() != null;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static final class UserPrincipal {
        private final Long userId;
        private final Integer role;

        public UserPrincipal(Long userId, Integer role) {
            this.userId = userId;
            this.role = role;
        }

        public Long getUserId() {
            return userId;
        }

        public Integer getRole() {
            return role;
        }
    }
}

