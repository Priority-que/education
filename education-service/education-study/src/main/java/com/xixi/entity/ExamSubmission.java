package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_submission")
public class ExamSubmission {
    
    /**
     * 提交ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 测验ID
     */
    private Long examId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 提交时间
     */
    private LocalDateTime submitTime;
    
    /**
     * 答案(JSON格式)
     */
    private String answers;
    
    /**
     * 总分
     */
    private BigDecimal totalScore;
    
    /**
     * 状态: IN_PROGRESS-进行中, PENDING_REVIEW-待人工批改, GRADED-已批改
     * 兼容旧值: SUBMITTED/AUTO_GRADED/MANUAL_GRADED
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

