package com.xixi.pojo.vo.admin;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 数据库监控概览。
 */
@Data
public class MonitorDatabaseOverviewVo {
    private Integer totalConnections;
    private Integer activeConnections;
    private BigDecimal connectionUsageRate;
    private Long slowQueryCount;
    private BigDecimal avgQueryTimeMs;
}
