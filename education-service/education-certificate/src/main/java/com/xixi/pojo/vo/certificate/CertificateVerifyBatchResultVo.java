package com.xixi.pojo.vo.certificate;

import lombok.Data;

import java.util.List;

/**
 * 7.3 批量验证结果。
 */
@Data
public class CertificateVerifyBatchResultVo {
    private Integer totalCount;
    private Integer validCount;
    private Integer invalidCount;
    private Integer revokedCount;
    private Integer expiredCount;
    private List<CertificateVerifyResultVo> results;
}

