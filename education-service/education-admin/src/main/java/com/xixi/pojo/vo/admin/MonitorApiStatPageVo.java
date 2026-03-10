package com.xixi.pojo.vo.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API 统计分页项。
 */
@Data
public class MonitorApiStatPageVo {
    private String apiPath;
    private String method;
    private Long totalCalls;
    private Long errorCalls;
    private BigDecimal avgResponseTime;
    private BigDecimal p95ResponseTime;
    private LocalDateTime reportTime;
}
