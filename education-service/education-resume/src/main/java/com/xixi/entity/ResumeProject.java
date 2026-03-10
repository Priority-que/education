package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("resume_project")
public class ResumeProject {
    
    /**
     * 项目经历ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 简历ID
     */
    private Long resumeId;
    
    /**
     * 项目名称
     */
    private String projectName;
    
    /**
     * 项目角色
     */
    private String projectRole;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 项目描述
     */
    private String projectDescription;
    
    /**
     * 使用技术
     */
    private String technologiesUsed;
    
    /**
     * 项目链接
     */
    private String projectLink;
    
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

