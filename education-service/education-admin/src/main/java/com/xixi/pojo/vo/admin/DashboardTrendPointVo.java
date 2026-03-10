package com.xixi.pojo.vo.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 看板趋势点视图对象。
 */
@Data
public class DashboardTrendPointVo {
    private LocalDate statDate;
    private BigDecimal value;
}
