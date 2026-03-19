package com.xixi.pojo.vo.certificate;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 7.5 验证报告详情。
 */
@Data
public class CertificateVerifyReportVo {
    private Long verificationId;
    private Long certificateId;
    private String certificateNumber;
    private String certificateName;
    private String certificateStatus;
    private LocalDate issuingDate;
    private LocalDate expiryDate;
    private String issuingAuthority;
    private String hash;
    private Long blockHeight;
    private String transactionHash;

    private Long verifierId;
    private String verifierType;
    private String verificationMethod;
    private String verificationResult;
    private LocalDateTime verificationTime;
    private String ipAddress;
    private String userAgent;
}

