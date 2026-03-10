package com.xixi.pojo.dto.certificate;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 8.1 教师手动颁发证书 DTO。
 */
@Data
public class CertificateIssueDto {
    private Long studentId;
    private Long courseId;
    /**
     * 可选，若传值需与当前登录教师一致。
     */
    private Long teacherId;
    private String certificateName;
    private String issuingAuthority;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate issuingDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    private String certificateContent;
    /**
     * JSON 字符串。
     */
    private String metadataJson;
    private String fileUrl;
    private String thumbnailUrl;
}

