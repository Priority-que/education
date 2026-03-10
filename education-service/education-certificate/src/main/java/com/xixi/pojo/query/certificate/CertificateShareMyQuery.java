package com.xixi.pojo.query.certificate;

import lombok.Data;

/**
 * 6.3 我的证书分享分页查询参数。
 */
@Data
public class CertificateShareMyQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private Long certificateId;
    /**
     * 0-失效，1-有效
     */
    private Integer isActive;
}

