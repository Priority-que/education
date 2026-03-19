package com.xixi.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测验VO
 */
@Data
public class ExamVo {
    
    /**
     * 测验ID
     */
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 教师ID
     */
    private Long teacherId;
    
    /**
     * 教师姓名
     */
    private String teacherName;
    
    /**
     * 测验标题
     */
    private String examTitle;
    
    /**
     * 测验描述
     */
    private String examDescription;
    
    /**
     * 测验类型
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
     * 时间限制(分钟)
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
     * 状态
     */
    private String status;
    
    /**
     * 参与状态（学生是否已参与）
     */
    private String participationStatus;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
