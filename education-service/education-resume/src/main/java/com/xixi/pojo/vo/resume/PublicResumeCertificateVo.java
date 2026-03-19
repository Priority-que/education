package com.xixi.pojo.vo.resume;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 企业端公开简历证书展示项。
 */
@Data
public class PublicResumeCertificateVo {
    /**
     * 简历证书关联ID。
     */
    private Long id;
    /**
     * 简历ID。
     */
    private Long resumeId;
    /**
     * 证书ID。
     */
    private Long certificateId;
    /**
     * 排序值。
     */
    private Integer sortOrder;
    /**
     * 关联创建时间。
     */
    private LocalDateTime createdTime;

    /**
     * 证书名称。
     */
    private String certificateName;
    /**
     * 证书编号。
     */
    private String certificateNumber;
    /**
     * 颁发机构。
     */
    private String issuingAuthority;
    /**
     * 颁发日期。
     */
    private LocalDate issuingDate;
    /**
     * 到期日期。
     */
    private LocalDate expiryDate;
    /**
     * 有效期展示文本，前端可直接渲染。
     */
    private String validityPeriodText;
    /**
     * 证书状态。
     */
    private String status;
    /**
     * 证书文件地址。
     */
    private String fileUrl;
    /**
     * 缩略图地址。
     */
    private String thumbnailUrl;
}
