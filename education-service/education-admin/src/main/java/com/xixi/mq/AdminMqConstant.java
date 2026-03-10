package com.xixi.mq;

/**
 * 管理服务 MQ 常量。
 */
public final class AdminMqConstant {
    public static final String ADMIN_EVENT_EXCHANGE = "education.admin.event.exchange";
    public static final String ADMIN_EVENT_QUEUE = "education.admin.event.queue";
    public static final String ADMIN_EVENT_ROUTING_KEY = "admin.event.#";
    public static final String ADMIN_EVENT_PUBLISH_ROUTING_KEY = "admin.event.changed";

    private AdminMqConstant() {
    }
}
