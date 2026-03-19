package com.xixi.mq;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 内部消息投递命令事件。
 */
@Data
public class InternalMessageSendCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final String EVENT_SEND_USER = "MESSAGE_SEND_USER";
    public static final String EVENT_SEND_USERS = "MESSAGE_SEND_USERS";
    public static final String EVENT_SEND_ROLE = "MESSAGE_SEND_ROLE";
    public static final String EVENT_SEND_TEMPLATE = "MESSAGE_SEND_TEMPLATE";

    private String eventId;
    private String eventType;
    private String traceId;
    private String sourceService;
    private LocalDateTime occurredAt;
    private InternalMessageSendPayload payload;
}

