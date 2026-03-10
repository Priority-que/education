package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("certificate_share")
public class CertificateShare {
    
    /**
     * 分享ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 证书ID
     */
    private Long certificateId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 分享令牌
     */
    private String shareToken;
    
    /**
     * 分享链接
     */
    private String shareUrl;
    
    /**
     * 二维码地址
     */
    private String qrCodeUrl;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiryTime;
    
    /**
     * 查看次数
     */
    private Integer viewCount;
    
    /**
     * 是否有效: 0-失效, 1-有效
     */
    private Boolean isActive;
    
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

