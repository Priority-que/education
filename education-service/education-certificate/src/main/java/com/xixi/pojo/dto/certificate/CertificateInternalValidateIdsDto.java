package com.xixi.pojo.dto.certificate;

import lombok.Data;

import java.util.List;

/**
 * 10.3 批量校验证书ID DTO（内部）。
 */
@Data
public class CertificateInternalValidateIdsDto {
    private Long studentId;
    private List<Long> certificateIds;
    /**
     * 是否要求证书状态为ISSUED，默认true。
     */
    private Boolean requireIssued = true;
}

