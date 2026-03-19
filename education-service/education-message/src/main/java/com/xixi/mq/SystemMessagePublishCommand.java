package com.xixi.mq;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统消息发布命令。
 */
@Data
public class SystemMessagePublishCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 命令唯一ID，用于幂等。
     */
    private String eventId;
    /**
     * 固定值：SYSTEM_MESSAGE_PUBLISH。
     */
    private String eventType;
    private Long systemMessageId;
    private Long operatorId;
    private Integer expectedCount;
    private LocalDateTime publishTime;
}
