package com.xixi.mq;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历证书关联变更事件。
 */
@Data
public class ResumeCertificateChangedEvent {
    private String eventId;
    private String eventType;
    private Long resumeCertificateId;
    private Long resumeId;
    private Long certificateId;
    private Long studentId;
    private Integer sortOrder;
    private LocalDateTime eventTime;
}
