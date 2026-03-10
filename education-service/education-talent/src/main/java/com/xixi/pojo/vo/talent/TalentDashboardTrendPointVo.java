package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDate;

/**
 * 企业看板趋势点数据。
 */
@Data
public class TalentDashboardTrendPointVo {
    private LocalDate statDate;
    private String metricType;
    private Integer metricValue;
}
