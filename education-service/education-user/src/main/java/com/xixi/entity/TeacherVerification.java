package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teacher_verification")
public class TeacherVerification {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teacherId;

    private String applicationNo;

    private String applyContent;

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
