package com.xixi.pojo.vo.certificate;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 8.3 教师已颁发证书分页项。
 */
@Data
public class CertificateTeacherIssuedVo {
    private Long id;
    private String certificateNumber;
    private Long studentId;
    private Long courseId;
    private Long teacherId;
    private String certificateName;
    private String issuingAuthority;
    private LocalDate issuingDate;
    private LocalDate expiryDate;
    private String status;
    private Integer verificationCount;
    private LocalDateTime lastVerificationTime;
    private LocalDateTime createdTime;
}

