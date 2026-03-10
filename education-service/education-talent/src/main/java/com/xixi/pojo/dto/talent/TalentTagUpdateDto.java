package com.xixi.pojo.dto.talent;

import lombok.Data;

/**
 * 更新标签参数。
 */
@Data
public class TalentTagUpdateDto {
    private String tagName;
    private String tagColor;
    private String description;
    private Integer sortOrder = 0;
}
