package com.xixi.mq;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 证书验证变更事件。
 */
@Data
public class CertificateVerificationChangedEvent {
    private String eventId;
    private String eventType;
    private Long verificationId;
    private Long certificateId;
    private String certificateNumber;
    private String verificationMethod;
    private String verificationResult;
    private String verifierType;
    private LocalDateTime eventTime;
}

