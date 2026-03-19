package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("grade")
public class Grade {
    
    /**
     * 成绩ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称(冗余)
     */
    private String courseName;
    
    /**
     * 教师ID(冗余)
     */
    private Long teacherId;
    
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
     * 成绩等级: A, B, C, D, F
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
     * 是否通过: 0-未通过, 1-通过
     */
    private Boolean isPass;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishedTime;
    
    /**
     * 发布教师ID
     */
    private Long publishedBy;
    
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

