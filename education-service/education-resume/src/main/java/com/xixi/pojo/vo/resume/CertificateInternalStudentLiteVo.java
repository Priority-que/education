package com.xixi.pojo.vo.resume;

import lombok.Data;

import java.time.LocalDate;

/**
 * 证书服务内部学生证书简化视图（用于简历服务聚合）。
 */
@Data
public class CertificateInternalStudentLiteVo {
    private Long id;
    private String certificateNumber;
    private String certificateName;
    private String issuingAuthority;
    private String status;
    private LocalDate issuingDate;
    private LocalDate expiryDate;
    private String fileUrl;
    private String thumbnailUrl;
}
