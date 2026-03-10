package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("resume_experience")
public class ResumeExperience {
    
    /**
     * 工作经历ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 简历ID
     */
    private Long resumeId;
    
    /**
     * 公司名称
     */
    private String companyName;
    
    /**
     * 职位
     */
    private String position;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 是否在职: 0-否, 1-是
     */
    private Boolean isCurrent;
    
    /**
     * 工作地点
     */
    private String location;
    
    /**
     * 工作描述
     */
    private String description;
    
    /**
     * 工作成就
     */
    private String achievements;
    
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

