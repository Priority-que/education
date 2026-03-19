package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业批注扩展信息。
 */
@Data
@TableName("homework_submission_annotation")
public class HomeworkSubmissionAnnotation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long submissionId;

    private String annotationMode;

    private String annotationContent;

    private String annotationDataJson;

    /**
     * JSON array string
     */
    private String annotationAttachments;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
