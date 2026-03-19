package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 区块链节点实体。
 */
@Data
@TableName("blockchain_node")
public class BlockchainNode {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String nodeName;

    private String endpoint;

    /**
     * ONLINE / OFFLINE / SYNCING
     */
    private String status;

    private Long blockHeight;

    private LocalDateTime lastSyncTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
