package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam")
public class Exam {
    
    /**
     * 测验ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 教师ID
     */
    private Long teacherId;
    
    /**
     * 测验标题
     */
    private String examTitle;
    
    /**
     * 测验描述
     */
    private String examDescription;
    
    /**
     * 测验类型: QUIZ-小测验, MIDTERM-期中, FINAL-期末
     */
    private String examType;
    
    /**
     * 总分
     */
    private Integer totalScore;
    
    /**
     * 及格分数
     */
    private Integer passScore;
    
    /**
     * 时间限制(分钟, 0表示不限)
     */
    private Integer timeLimit;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 状态: DRAFT-草稿, PUBLISHED-已发布, IN_PROGRESS-进行中, ENDED-已结束
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

