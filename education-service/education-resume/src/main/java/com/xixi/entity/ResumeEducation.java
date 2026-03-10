package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("resume_education")
public class ResumeEducation {
    
    /**
     * 教育经历ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 简历ID
     */
    private Long resumeId;
    
    /**
     * 学校名称
     */
    private String schoolName;
    
    /**
     * 学位: BACHELOR-学士, MASTER-硕士, DOCTOR-博士, OTHER-其他
     */
    private String degree;
    
    /**
     * 专业
     */
    private String major;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * GPA
     */
    private BigDecimal gpa;
    
    /**
     * 排名
     */
    private String ranking;
    
    /**
     * 荣誉奖项
     */
    private String honors;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
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

