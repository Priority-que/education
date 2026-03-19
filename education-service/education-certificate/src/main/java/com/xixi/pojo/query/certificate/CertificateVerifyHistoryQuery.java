package com.xixi.pojo.query.certificate;

import lombok.Data;

/**
 * 7.4 验证历史分页查询参数。
 */
@Data
public class CertificateVerifyHistoryQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String verificationResult;
    private String verificationMethod;
}

