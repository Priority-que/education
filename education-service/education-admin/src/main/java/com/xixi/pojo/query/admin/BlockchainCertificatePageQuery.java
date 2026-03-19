package com.xixi.pojo.query.admin;

import lombok.Data;

/**
 * 管理端区块链证书分页查询参数。
 */
@Data
public class BlockchainCertificatePageQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String certificateNumber;
    private String status;
    private Long blockHeight;
}
