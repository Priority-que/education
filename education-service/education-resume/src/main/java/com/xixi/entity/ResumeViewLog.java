package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("resume_view_log")
public class ResumeViewLog {
    
    /**
     * 查看记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 简历ID
     */
    private Long resumeId;
    
    /**
     * 查看者ID
     */
    private Long viewerId;
    
    /**
     * 查看者类型: ENTERPRISE-企业, ADMIN-管理员, STUDENT-学生
     */
    private String viewerType;
    
    /**
     * 查看时间
     */
    private LocalDateTime viewTime;
    
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

