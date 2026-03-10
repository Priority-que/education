package com.xixi.pojo.vo.certificate;

import lombok.Data;

/**
 * 管理端证书上链结果。
 */
@Data
public class CertificateAnchorResultVo {
    private Long certificateId;
    private String certificateNumber;
    private Long blockHeight;
    private String transactionHash;
    private String previousHash;
    private String currentHash;
    private Boolean alreadyAnchored;
}

