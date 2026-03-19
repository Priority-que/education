package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("communication_record")
public class CommunicationRecord {
    
    /**
     * 沟通记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 企业ID
     */
    private Long enterpriseId;
    
    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 关联投递记录ID
     */
    private Long applicationId;

    /**
     * 关联岗位ID
     */
    private Long jobId;
    
    /**
     * 沟通类型: EMAIL-邮件, PHONE-电话, INTERVIEW-面试, MESSAGE-站内信
     */
    private String communicationType;
    
    /**
     * 沟通主题
     */
    private String communicationSubject;
    
    /**
     * 沟通内容
     */
    private String communicationContent;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者类型: ENTERPRISE-企业, STUDENT-学生
     */
    private String senderType;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 接收者类型
     */
    private String receiverType;
    
    /**
     * 附件地址
     */
    private String attachmentUrl;

    /**
     * 是否需要学生确认
     */
    private Boolean needStudentConfirm;

    /**
     * 学生是否已确认
     */
    private Boolean studentConfirmed;

    /**
     * 学生确认时间
     */
    private LocalDateTime confirmTime;

    /**
     * 学生确认备注
     */
    private String confirmRemark;

    /**
     * 面试时间
     */
    private LocalDateTime interviewTime;

    /**
     * 面试地点
     */
    private String interviewAddress;
    
    /**
     * 是否已读: 0-未读, 1-已读
     */
    private Boolean isRead;
    
    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
    
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

