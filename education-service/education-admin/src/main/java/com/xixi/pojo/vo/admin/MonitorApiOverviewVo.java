package com.xixi.pojo.vo.admin;

import lombok.Data;

import java.math.BigDecimal;

/**
 * API 调用概览。
 */
@Data
public class MonitorApiOverviewVo {
    private Long totalCalls;
    private Long errorCalls;
    private BigDecimal qps;
    private BigDecimal avgResponseTime;
}
