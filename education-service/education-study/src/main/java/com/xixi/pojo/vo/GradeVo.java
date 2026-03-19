package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩VO
 */
@Data
public class GradeVo {
    
    /**
     * 成绩ID
     */
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号
     */
    private String studentNumber;

    /**
     * 学生姓名
     */
    private String studentName;
    
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
     * 考勤成绩
     */
    private BigDecimal attendanceScore;
    
    /**
     * 作业成绩
     */
    private BigDecimal homeworkScore;
    
    /**
     * 测验成绩
     */
    private BigDecimal quizScore;
    
    /**
     * 考试成绩
     */
    private BigDecimal examScore;
    
    /**
     * 最终成绩
     */
    private BigDecimal finalScore;
    
    /**
     * 成绩等级
     */
    private String gradeLevel;
    
    /**
     * 绩点
     */
    private BigDecimal gpa;
    
    /**
     * 获得学分
     */
    private BigDecimal creditEarned;
    
    /**
     * 是否通过
     */
    private Boolean isPass;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishedTime;

    /**
     * 是否已发布
     */
    private Boolean published;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
















