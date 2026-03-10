package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位投递状态流水实体。
 */
@Data
@TableName("job_application_status_log")
public class JobApplicationStatusLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId;

    private String applicationNo;

    private Long jobId;

    private Long studentId;

    private Long enterpriseId;

    private String actionType;

    private String fromStatus;

    private String toStatus;

    private Long relatedId;

    private Long operatorUserId;

    private Integer operatorRole;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
