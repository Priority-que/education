package com.xixi.pojo.vo.certificate;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 7.1/7.2 单次验证结果。
 */
@Data
public class CertificateVerifyResultVo {
    private Long verificationId;
    private String verificationMethod;
    private String verificationResult;
    private LocalDateTime verificationTime;
    private String verifierType;

    private Long certificateId;
    private String certificateNumber;
    private String certificateName;
    private String certificateStatus;
    private LocalDate issuingDate;
    private LocalDate expiryDate;
    private String hash;
    private Long blockHeight;
    private String transactionHash;

    private String message;
}

