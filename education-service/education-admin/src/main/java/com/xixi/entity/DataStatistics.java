package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("data_statistics")
public class DataStatistics {
    
    /**
     * 统计ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 统计日期
     */
    private LocalDate statDate;
    
    /**
     * 总用户数
     */
    private Integer totalUsers;
    
    /**
     * 新增用户数
     */
    private Integer newUsers;
    
    /**
     * 活跃用户数
     */
    private Integer activeUsers;
    
    /**
     * 总学生数
     */
    private Integer totalStudents;
    
    /**
     * 总教师数
     */
    private Integer totalTeachers;
    
    /**
     * 总企业数
     */
    private Integer totalEnterprises;
    
    /**
     * 总课程数
     */
    private Integer totalCourses;
    
    /**
     * 新增课程数
     */
    private Integer newCourses;
    
    /**
     * 总证书数
     */
    private Integer totalCertificates;
    
    /**
     * 新增证书数
     */
    private Integer newCertificates;
    
    /**
     * 总简历数
     */
    private Integer totalResumes;
    
    /**
     * 总职位数
     */
    private Integer totalJobs;
    
    /**
     * 总学习时长(秒)
     */
    private Long totalStudyTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}

