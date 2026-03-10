package com.xixi.pojo.vo.admin;

import lombok.Data;

/**
 * 区块链证书分页项视图对象。
 */
@Data
public class BlockchainCertificatePageVo {
    private Long certificateId;
    private String certificateNumber;
    private String status;
    private Long blockHeight;
    private String transactionHash;
    private Boolean anchored;
}
