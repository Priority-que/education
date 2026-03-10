package com.xixi.pojo.query.admin;

import lombok.Data;

/**
 * 区块链节点分页查询参数。
 */
@Data
public class BlockchainNodePageQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String keyword;
    private String status;
}
