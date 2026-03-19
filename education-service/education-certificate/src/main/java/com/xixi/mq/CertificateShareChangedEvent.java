package com.xixi.mq;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 证书分享变更事件。
 */
@Data
public class CertificateShareChangedEvent {
    private String eventId;
    private String eventType;
    private Long shareId;
    private Long certificateId;
    private Long studentId;
    private String shareToken;
    private Integer viewCount;
    private LocalDateTime eventTime;
}

