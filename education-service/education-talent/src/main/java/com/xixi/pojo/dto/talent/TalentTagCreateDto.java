package com.xixi.pojo.dto.talent;

import lombok.Data;

/**
 * 创建标签参数。
 */
@Data
public class TalentTagCreateDto {
    private String tagName;
    private String tagColor;
    private String description;
    private Integer sortOrder = 0;
}
