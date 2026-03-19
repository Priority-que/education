package com.xixi.pojo.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 区块链节点视图对象。
 */
@Data
public class BlockchainNodeVo {
    private Long nodeId;
    private String nodeName;
    private String endpoint;
    private String status;
    private Long blockHeight;
    private LocalDateTime lastSyncTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
