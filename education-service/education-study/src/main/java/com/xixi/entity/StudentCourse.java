package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("student_course")
public class StudentCourse {
    
    /**
     * 选课ID
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
     * 教师姓名(冗余)
     */
    private String teacherName;
    
    /**
     * 选课时间
     */
    private LocalDateTime selectedTime;
    
    /**
     * 学习状态: STUDYING-学习中, COMPLETED-已完成, DROPPED-已退课
     */
    private String learningStatus;
    
    /**
     * 学习进度百分比
     */
    private BigDecimal progressPercentage;
    
    /**
     * 总学习时长(秒)
     */
    private Integer totalStudyTime;
    
    /**
     * 最后学习时间
     */
    private LocalDateTime lastStudyTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
    
    /**
     * 学生评分
     */
    private BigDecimal rating;
    
    /**
     * 学习心得
     */
    private String reviewContent;
    
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

