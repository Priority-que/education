package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("certificate_verification")
public class CertificateVerification {
    
    /**
     * 验证ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 证书ID
     */
    private Long certificateId;
    
    /**
     * 证书编号
     */
    private String certificateNumber;
    
    /**
     * 验证者ID
     */
    private Long verifierId;
    
    /**
     * 验证者类型: ENTERPRISE-企业, ADMIN-管理员, PUBLIC-公众
     */
    private String verifierType;
    
    /**
     * 验证方式: NUMBER-编号验证, QRCODE-二维码, HASH-哈希验证
     */
    private String verificationMethod;
    
    /**
     * 验证结果: VALID-有效, INVALID-无效, REVOKED-已撤销, EXPIRED-已过期
     */
    private String verificationResult;
    
    /**
     * 验证时间
     */
    private LocalDateTime verificationTime;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}

