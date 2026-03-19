package com.xixi.pojo.vo.certificate;

import lombok.Data;

import java.util.List;

/**
 * 10.3 批量校验证书ID结果（内部）。
 */
@Data
public class CertificateInternalValidateIdsVo {
    private Integer totalCount;
    private Integer validCount;
    private Integer invalidCount;
    private List<Long> validIds;
    private List<CertificateIdValidationItemVo> invalidItems;
}

