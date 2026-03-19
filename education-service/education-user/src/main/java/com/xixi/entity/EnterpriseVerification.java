package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业认证申请实体。
 */
@Data
@TableName("enterprise_verification")
public class EnterpriseVerification {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long enterpriseId;

    private String applicationNo;

    private String applyContent;

    /**
     * PENDING / APPROVED / REJECTED
     */
    private String status;

    private String auditReason;

    private LocalDateTime submittedTime;

    private LocalDateTime auditedTime;

    private Long auditorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
