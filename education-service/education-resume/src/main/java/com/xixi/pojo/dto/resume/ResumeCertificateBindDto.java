package com.xixi.pojo.dto.resume;

import lombok.Data;

/**
 * 简历证书绑定请求参数。
 */
@Data
public class ResumeCertificateBindDto {
    /**
     * 简历ID。
     */
    private Long resumeId;

    /**
     * 证书ID。
     */
    private Long certificateId;

    /**
     * 排序值，不传则自动补位。
     */
    private Integer sortOrder;
}
