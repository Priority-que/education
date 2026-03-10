package com.xixi.pojo.query.certificate;

import lombok.Data;

/**
 * 5.1 我的证书分页查询参数。
 */
@Data
public class CertificateMyQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String status;
    private Long courseId;
    private String keyword;
}

