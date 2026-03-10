package com.xixi.mq;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公开简历访问事件。
 */
@Data
public class ResumePublicAccessEvent {
    private String eventId;
    private String eventType;
    private Long resumeId;
    private Long viewerId;
    private String viewerType;
    private String keyword;
    private String major;
    private String degree;
    private Integer pageNum;
    private Integer pageSize;
    private LocalDateTime eventTime;
}
