package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("resume_skill")
public class ResumeSkill {
    
    /**
     * 技能ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 简历ID
     */
    private Long resumeId;
    
    /**
     * 技能名称
     */
    private String skillName;
    
    /**
     * 技能分类: LANGUAGE-语言, FRAMEWORK-框架, DATABASE-数据库, TOOL-工具, OTHER-其他
     */
    private String skillCategory;
    
    /**
     * 熟练程度: BEGINNER-初学者, INTERMEDIATE-中等, ADVANCED-高级, EXPERT-专家
     */
    private String proficiencyLevel;
    
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

