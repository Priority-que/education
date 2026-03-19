package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业联系人管理实体。
 */
@Data
@TableName("talent_contact")
public class TalentContact {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long enterpriseId;

    private Long studentId;

    private String sourceType;

    private Long applicationId;

    private Long jobId;

    private String name;

    private String phone;

    private String email;

    private String wechat;

    private String position;

    private String status;

    private String latestStatus;

    private LocalDateTime lastContactTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
