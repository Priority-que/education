package com.xixi.mq;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 我的消息变更事件。
 */
@Data
public class UserMessageChangedEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final String EVENT_TYPE = "USER_MESSAGE_CHANGED";

    private String eventId;
    private String eventType;
    /**
     * READ_ONE/READ_BATCH/READ_ALL/DELETE_ONE/DELETE_BATCH
     */
    private String action;
    private Long userId;
    private String messageType;
    private List<Long> messageIds;
    private Integer affectedCount;
    private LocalDateTime occurredAt;
}

