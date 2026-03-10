package com.xixi.pojo.vo.certificate;

import lombok.Data;

/**
 * 10.3 单个证书ID校验结果（内部）。
 */
@Data
public class CertificateIdValidationItemVo {
    private Long certificateId;
    private Boolean valid;
    private String reason;
    private Long ownerStudentId;
    private String status;
}

