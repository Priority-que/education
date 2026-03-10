package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教师端选课学生VO
 */
@Data
public class TeacherStudentCourseVo {
    /**
     * 选课ID
     */
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 学号
     */
    private String studentNumber;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 学生昵称
     */
    private String nickname;
    
    /**
     * 学生头像
     */
    private String avatar;
    
    /**
     * 学生邮箱
     */
    private String email;
    
    /**
     * 学生手机号
     */
    private String phone;
    
    /**
     * 学校
     */
    private String school;
    
    /**
     * 学院
     */
    private String college;
    
    /**
     * 专业
     */
    private String major;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
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
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
















