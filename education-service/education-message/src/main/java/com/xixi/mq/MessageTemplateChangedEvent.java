package com.xixi.mq;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模板变更事件，用于通过MQ异步通知。
 */
@Data
public class MessageTemplateChangedEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 全局事件ID，便于幂等处理。
     */
    private String eventId;
    /**
     * 事件类型，固定值：MESSAGE_TEMPLATE_CHANGED。
     */
    private String eventType;
    /**
     * 变更动作：CREATE/UPDATE/STATUS_CHANGE/DELETE。
     */
    private String action;
    private Long operatorId;
    private Long templateId;
    private String templateCode;
    private LocalDateTime occurredAt;
}
