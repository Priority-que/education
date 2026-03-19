package com.xixi.mq;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人才域事件消息体。
 */
@Data
public class TalentDomainEvent {
    private String eventId;
    private String eventType;
    private Long enterpriseId;
    private Long bizId;
    private String payload;
    private LocalDateTime eventTime;
}
