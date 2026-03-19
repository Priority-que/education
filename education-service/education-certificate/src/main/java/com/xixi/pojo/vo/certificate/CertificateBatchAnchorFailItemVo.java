package com.xixi.pojo.vo.certificate;

import lombok.Data;

/**
 * 批量上链失败项。
 */
@Data
public class CertificateBatchAnchorFailItemVo {
    private Long certificateId;
    private Integer code;
    private String message;
}

