package com.xixi.mq;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 证书上链完成事件。
 */
@Data
public class CertificateBlockchainAnchoredEvent {
    private String eventId;
    private String eventType;
    private Long certificateId;
    private String certificateNumber;
    private Long blockHeight;
    private String transactionHash;
    private String currentHash;
    private Long operatorId;
    private LocalDateTime eventTime;
}

