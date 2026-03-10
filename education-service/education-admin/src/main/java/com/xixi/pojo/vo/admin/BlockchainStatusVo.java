package com.xixi.pojo.vo.admin;

import lombok.Data;

/**
 * 区块链状态视图对象。
 */
@Data
public class BlockchainStatusVo {
    private Integer totalAttempts;
    private Integer successCount;
    private Integer failedCount;
    private Long latestBlockHeight;
}
