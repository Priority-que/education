package com.xixi.pojo.dto.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 系统监控上报请求参数。
 */
@Data
public class MonitorReportDto {
    private String serverName;
    private String serviceName;
    private BigDecimal cpuUsage;
    private BigDecimal memoryUsage;
    private BigDecimal diskUsage;
    private Long heapMemory;
    private Long nonHeapMemory;
    private Integer threadCount;
    private Long jvmUptime;
    private Integer gcCount;
    private Integer requestCount;
    private Integer errorCount;
    private BigDecimal avgResponseTime;
    private String status;
    private LocalDateTime reportTime;
}
