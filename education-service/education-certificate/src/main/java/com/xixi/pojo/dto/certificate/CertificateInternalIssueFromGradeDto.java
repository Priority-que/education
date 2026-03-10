package com.xixi.pojo.dto.certificate;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 10.1 成绩达标触发发证 DTO（内部）。
 */
@Data
public class CertificateInternalIssueFromGradeDto {
    private Long studentId;
    private Long courseId;
    private Long teacherId;
    private String certificateName;
    private String issuingAuthority;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate issuingDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    private BigDecimal finalScore;
    /**
     * 可选，优先使用该字段。
     */
    private String metadataJson;
    private String certificateContent;
    private String fileUrl;
    private String thumbnailUrl;
}

