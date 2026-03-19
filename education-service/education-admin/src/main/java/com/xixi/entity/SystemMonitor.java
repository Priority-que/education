package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("system_monitor")
public class SystemMonitor {
    
    /**
     * 监控ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 服务器名称
     */
    private String serverName;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * CPU使用率
     */
    private BigDecimal cpuUsage;
    
    /**
     * 内存使用率
     */
    private BigDecimal memoryUsage;
    
    /**
     * 磁盘使用率
     */
    private BigDecimal diskUsage;
    
    /**
     * 堆内存
     */
    private Long heapMemory;
    
    /**
     * 非堆内存
     */
    private Long nonHeapMemory;
    
    /**
     * 线程数
     */
    private Integer threadCount;
    
    /**
     * JVM运行时间
     */
    private Long jvmUptime;
    
    /**
     * GC次数
     */
    private Integer gcCount;
    
    /**
     * 请求数
     */
    private Integer requestCount;
    
    /**
     * 错误数
     */
    private Integer errorCount;
    
    /**
     * 平均响应时间
     */
    private BigDecimal avgResponseTime;
    
    /**
     * 状态: UP-正常, DOWN-异常, WARNING-警告
     */
    private String status;
    
    /**
     * 报告时间
     */
    private LocalDateTime reportTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}

