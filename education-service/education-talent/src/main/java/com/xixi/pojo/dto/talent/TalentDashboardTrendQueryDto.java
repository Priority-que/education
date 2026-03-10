package com.xixi.pojo.dto.talent;

import lombok.Data;

import java.time.LocalDate;

/**
 * 企业看板趋势查询参数。
 */
@Data
public class TalentDashboardTrendQueryDto {
    private Long enterpriseId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String metricType;
}
