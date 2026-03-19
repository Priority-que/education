package com.xixi.pojo.vo.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 慢 SQL 分页项。
 */
@Data
public class MonitorSlowSqlPageVo {
    private String sqlDigest;
    private BigDecimal avgTimeMs;
    private BigDecimal maxTimeMs;
    private Long executeCount;
    private LocalDateTime reportTime;
}
