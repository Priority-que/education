package com.xixi.mq;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历浏览日志变更事件。
 */
@Data
public class ResumeViewLogChangedEvent {
    private String eventId;
    private String eventType;
    private Long viewLogId;
    private Long resumeId;
    private Long viewerId;
    private String viewerType;
    private LocalDateTime viewTime;
    private LocalDateTime eventTime;
}
