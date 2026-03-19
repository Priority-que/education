package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测验提交VO
 */
@Data
public class ExamSubmissionVo {
    
    /**
     * 提交ID
     */
    private Long id;
    
    /**
     * 测验ID
     */
    private Long examId;
    
    /**
     * 测验标题
     */
    private String examTitle;

    /**
     * 测验类型
     */
    private String examType;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学号
     */
    private String studentNumber;
    
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
     * 状态
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
