package com.xixi.pojo.dto.resume;

import lombok.Data;

/**
 * 技能修改参数。
 */
@Data
public class ResumeSkillUpdateDto {
    private Long id;
    private String skillName;
    private String skillCategory;
    private String proficiencyLevel;
    private String description;
    private Integer sortOrder;
}
