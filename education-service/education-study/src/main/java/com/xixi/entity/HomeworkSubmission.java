package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("homework_submission")
public class HomeworkSubmission {
    
    /**
     * 提交ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 作业ID
     */
    private Long homeworkId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 提交内容
     */
    private String submissionContent;
    
    /**
     * 附件地址
     */
    private String attachmentUrl;
    
    /**
     * 提交时间
     */
    private LocalDateTime submissionTime;
    
    /**
     * 得分
     */
    private BigDecimal score;
    
    /**
     * 教师反馈
     */
    private String feedback;
    
    /**
     * 批改教师ID
     */
    private Long gradedBy;
    
    /**
     * 批改时间
     */
    private LocalDateTime gradedTime;
    
    /**
     * 状态: DRAFT-草稿, SUBMITTED-已提交, GRADED-已批改, LATE-迟交
     */
    private String status;
    
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

