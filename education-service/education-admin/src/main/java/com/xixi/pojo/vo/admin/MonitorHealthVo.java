package com.xixi.pojo.vo.admin;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 系统健康总览视图对象。
 */
@Data
public class MonitorHealthVo {
    private Integer totalServices;
    private Integer upCount;
    private Integer downCount;
    private Integer warningCount;
    private BigDecimal avgResponseTime;
}
