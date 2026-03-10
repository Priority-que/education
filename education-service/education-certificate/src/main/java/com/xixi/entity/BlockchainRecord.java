package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("blockchain_record")
public class BlockchainRecord {
    
    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 区块高度
     */
    private Long blockHeight;
    
    /**
     * 前一个区块哈希
     */
    private String previousHash;
    
    /**
     * 当前区块哈希
     */
    private String currentHash;
    
    /**
     * 证书哈希
     */
    private String certificateHash;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 随机数
     */
    private Long nonce;
    
    /**
     * 默克尔根
     */
    private String merkleRoot;
    
    /**
     * 区块大小
     */
    private Integer blockSize;
    
    /**
     * 交易数量
     */
    private Integer transactionCount;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}

