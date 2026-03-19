package com.xixi.context;

/**
 * 单次请求内的操作日志跟踪上下文。
 * 用于标记“本次请求是否已经手工记录过日志”，避免 AOP 重复写入。
 */
public final class OperationLogTraceContext {
    private static final ThreadLocal<Boolean> MANUAL_LOGGED = new ThreadLocal<>();

    private OperationLogTraceContext() {
    }

    public static void markManualLogged() {
        MANUAL_LOGGED.set(Boolean.TRUE);
    }

    public static boolean isManualLogged() {
        return Boolean.TRUE.equals(MANUAL_LOGGED.get());
    }

    public static void clear() {
        MANUAL_LOGGED.remove();
    }
}

