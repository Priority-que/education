package com.xixi.mq;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 证书主档变更事件。
 */
@Data
public class CertificateChangedEvent {
    private String eventId;
    private String eventType;
    private Long certificateId;
    private String certificateNumber;
    private Long studentId;
    private Long teacherId;
    private String status;
    private LocalDateTime eventTime;
}

