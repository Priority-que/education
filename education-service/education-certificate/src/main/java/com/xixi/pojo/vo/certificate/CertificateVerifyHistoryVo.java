package com.xixi.pojo.vo.certificate;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 7.4 验证历史分页项。
 */
@Data
public class CertificateVerifyHistoryVo {
    private Long id;
    private Long certificateId;
    private String certificateNumber;
    private String certificateName;
    private Long verifierId;
    private String verifierType;
    private String verificationMethod;
    private String verificationResult;
    private LocalDateTime verificationTime;
    private String ipAddress;
}

