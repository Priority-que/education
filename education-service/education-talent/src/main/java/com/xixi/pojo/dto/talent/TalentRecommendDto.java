package com.xixi.pojo.dto.talent;

import lombok.Data;

/**
 * 智能推荐请求参数。
 */
@Data
public class TalentRecommendDto {
    private String keyword;
    private String educationRequirement;
    private String skillTag;
    private String expectedSalary;
    private String city;
    private Integer pageSize = 10;
}
