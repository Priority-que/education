package com.xixi.pojo.vo.certificate;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 10.2 学生证书列表项（内部）。
 */
@Data
public class CertificateInternalStudentVo {
    private Long id;
    private String certificateNumber;
    private Long studentId;
    private Long courseId;
    private Long teacherId;
    private String certificateName;
    private String issuingAuthority;
    private String fileUrl;
    private String thumbnailUrl;
    private String status;
    private LocalDate issuingDate;
    private LocalDate expiryDate;
    private String hash;
    private Long blockHeight;
    private String transactionHash;
    private Integer verificationCount;
    private LocalDateTime lastVerificationTime;
    private LocalDateTime createdTime;
}
