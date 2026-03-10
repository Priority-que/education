package com.xixi.pojo.dto.talent;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 人才搜索分页查询参数。
 */
@Data
public class TalentSearchQueryDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private String educationRequirement;
    private String skillTag;
    private String certificateType;
    private String expectedSalary;
    private String city;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer radiusKm;
}
