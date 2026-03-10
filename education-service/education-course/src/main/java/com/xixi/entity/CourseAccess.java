package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程访问控制扩展实体。
 */
@Data
@TableName("course_access")
public class CourseAccess {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    /**
     * FREE / PAID / PASSWORD / PAID_AND_PASSWORD
     */
    private String accessType;

    private String passwordHash;

    private String passwordHint;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
