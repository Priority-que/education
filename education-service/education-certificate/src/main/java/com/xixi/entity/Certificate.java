package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("certificate")
public class Certificate {
    
    /**
     * 证书ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 证书编号
     */
    private String certificateNumber;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 教师ID(冗余)
     */
    private Long teacherId;
    
    /**
     * 证书名称
     */
    private String certificateName;
    
    /**
     * 颁发机构
     */
    private String issuingAuthority;
    
    /**
     * 颁发日期
     */
    private LocalDate issuingDate;
    
    /**
     * 过期日期
     */
    private LocalDate expiryDate;
    
    /**
     * 证书内容
     */
    private String certificateContent;
    
    /**
     * 元数据(JSON格式)
     */
    private String metadataJson;
    
    /**
     * 证书文件地址
     */
    private String fileUrl;
    
    /**
     * 缩略图地址
     */
    private String thumbnailUrl;
    
    /**
     * 证书哈希值
     */
    private String hash;
    
    /**
     * 上一个区块哈希
     */
    private String previousHash;
    
    /**
     * 状态: ISSUED-已颁发, REVOKED-已撤销, EXPIRED-已过期
     */
    private String status;
    
    /**
     * 区块链高度
     */
    private Long blockHeight;
    
    /**
     * 交易哈希
     */
    private String transactionHash;
    
    /**
     * 验证次数
     */
    private Integer verificationCount;
    
    /**
     * 最后验证时间
     */
    private LocalDateTime lastVerificationTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}

