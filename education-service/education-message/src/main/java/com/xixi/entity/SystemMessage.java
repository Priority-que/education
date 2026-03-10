package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("system_message")
public class SystemMessage {
    
    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 消息类型: NOTICE-通知, REMINDER-提醒, ANNOUNCEMENT-公告
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
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者姓名
     */
    private String senderName;
    
    /**
     * 优先级: 0-普通, 1-重要, 2-紧急
     */
    private Integer priority;
    
    /**
     * 目标类型: ALL-所有用户, ROLE-角色, USER-指定用户
     */
    private String targetType;
    
    /**
     * 目标值(JSON数组)
     */
    private String targetValue;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiryTime;
    
    /**
     * 状态: DRAFT-草稿, PUBLISHED-已发布, WITHDRAWN-已撤回
     */
    private String status;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
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

