package com.xixi.pojo.query.admin;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 监控告警分页查询参数。
 */
@Data
public class MonitorAlertPageQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private BigDecimal minCpuUsage;
    private BigDecimal minMemoryUsage;
    /**
     * 错误率阈值，0~1。
     */
    private BigDecimal minErrorRate;
    private String status;
}
