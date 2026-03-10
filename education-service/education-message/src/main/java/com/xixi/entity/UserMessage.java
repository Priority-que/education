package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_message")
public class UserMessage {
    
    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 消息类型: SYSTEM-系统消息, COURSE-课程相关, CERTIFICATE-证书相关, JOB-职位相关, OTHER-其他
     */
    private String messageType;
    
    /**
     * 消息标题
     */
    private String messageTitle;
    
    /**
     * 消息内容
     */
    private String messageContent;
    
    /**
     * 关联ID
     */
    private Long relatedId;
    
    /**
     * 关联类型
     */
    private String relatedType;
    
    /**
     * 是否已读: 0-未读, 1-已读
     */
    private Boolean isRead;
    
    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
    
    /**
     * 优先级
     */
    private Integer priority;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiryTime;
    
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

