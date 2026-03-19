package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_template")
public class MessageTemplate {
    
    /**
     * 模板ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板代码
     */
    private String templateCode;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 模板类型: EMAIL-邮件, SMS-短信, NOTIFICATION-站内通知
     */
    private String templateType;
    
    /**
     * 模板主题
     */
    private String templateSubject;
    
    /**
     * 模板内容
     */
    private String templateContent;
    
    /**
     * 变量定义
     */
    private String variables;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 状态: 0-禁用, 1-启用
     */
    private Boolean status;
    
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

