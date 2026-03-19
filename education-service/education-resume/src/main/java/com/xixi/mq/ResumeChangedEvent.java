package com.xixi.mq;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历主档变更事件。
 */
@Data
public class ResumeChangedEvent {
    private String eventId;
    private String eventType;
    private Long resumeId;
    private Long studentId;
    private String visibility;
    private Boolean isDefault;
    private LocalDateTime eventTime;
}
