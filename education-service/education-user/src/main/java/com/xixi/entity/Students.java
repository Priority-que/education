package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("students")
public class Students {
    
    /**
     * 学生ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 学号
     */
    private String studentNumber;
    
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
     * 入学年份
     */
    private String enrollmentYear;
    
    /**
     * 预计毕业时间
     */
    private String expectedGraduation;
    
    /**
     * GPA
     */
    private BigDecimal gpa;
    
    /**
     * 总学分
     */
    private Integer totalCredits;
    
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

