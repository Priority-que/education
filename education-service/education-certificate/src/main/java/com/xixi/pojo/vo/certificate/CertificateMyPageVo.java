package com.xixi.pojo.vo.certificate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 5.1 我的证书分页项。
 */
@Data
public class CertificateMyPageVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String certificateNumber;
    private Long courseId;
    private String certificateName;
    private String issuingAuthority;
    private LocalDate issuingDate;
    private LocalDate expiryDate;
    private String status;
    private Integer verificationCount;
    private LocalDateTime lastVerificationTime;
    private LocalDateTime createdTime;
}
