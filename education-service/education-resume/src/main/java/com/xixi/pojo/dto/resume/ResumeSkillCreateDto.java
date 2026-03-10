package com.xixi.pojo.dto.resume;

import lombok.Data;

/**
 * 技能新增参数。
 */
@Data
public class ResumeSkillCreateDto {
    private Long resumeId;
    private String skillName;
    private String skillCategory;
    private String proficiencyLevel;
    private String description;
    private Integer sortOrder;
}
