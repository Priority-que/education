package com.xixi.mq;

/**
 * 消息服务 RabbitMQ 资源常量。
 */
public final class MessageMqConstant {
    public static final String MESSAGE_COMMAND_EXCHANGE = "education.message.command.exchange";
    public static final String MESSAGE_COMMAND_QUEUE = "education.message.command.queue";
    public static final String MESSAGE_COMMAND_ROUTING_KEY_PATTERN = "message.command.#";

    public static final String MESSAGE_RETRY_EXCHANGE = "education.message.retry.exchange";
    public static final String MESSAGE_RETRY_QUEUE = "education.message.retry.queue";
    public static final String MESSAGE_RETRY_ROUTING_KEY_PATTERN = "message.retry.#";

    public static final String MESSAGE_DLX_EXCHANGE = "education.message.dlx.exchange";
    public static final String MESSAGE_DLX_QUEUE = "education.message.dlx.queue";
    public static final String MESSAGE_DLX_ROUTING_KEY_PATTERN = "message.dlx.#";

    public static final String TEMPLATE_CHANGE_ROUTING_KEY = "message.command.template.change";
    public static final String SYSTEM_MESSAGE_PUBLISH_ROUTING_KEY = "message.command.system.publish";
    public static final String SYSTEM_MESSAGE_PUBLISH_QUEUE = "education.message.system.publish.queue";
    public static final String INTERNAL_SEND_QUEUE = "education.message.internal.send.queue";
    public static final String INTERNAL_SEND_ROUTING_KEY_PATTERN = "message.command.send.#";
    public static final String SEND_USER_ROUTING_KEY = "message.command.send.user";
    public static final String SEND_USERS_ROUTING_KEY = "message.command.send.users";
    public static final String SEND_ROLE_ROUTING_KEY = "message.command.send.role";
    public static final String SEND_TEMPLATE_ROUTING_KEY = "message.command.send.template";
    public static final String USER_MESSAGE_CHANGED_QUEUE = "education.message.user.changed.queue";
    public static final String USER_MESSAGE_CHANGED_ROUTING_KEY = "message.command.user.changed";

    private MessageMqConstant() {
    }
}
