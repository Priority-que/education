package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audit_record")
public class AuditRecord {
    
    /**
     * 审核记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 审核类型: COURSE-课程, CERTIFICATE-证书, ENTERPRISE-企业, TEACHER-教师
     */
    private String auditType;
    
    /**
     * 目标ID
     */
    private Long targetId;
    
    /**
     * 目标名称
     */
    private String targetName;
    
    /**
     * 申请人ID
     */
    private Long applicantId;
    
    /**
     * 申请人姓名
     */
    private String applicantName;
    
    /**
     * 审核人ID
     */
    private Long auditorId;
    
    /**
     * 审核人姓名
     */
    private String auditorName;
    
    /**
     * 审核状态: PENDING-待审核, APPROVED-通过, REJECTED-拒绝
     */
    private String auditStatus;
    
    /**
     * 审核意见
     */
    private String auditOpinion;
    
    /**
     * 拒绝原因
     */
    private String rejectReason;
    
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    
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

