package com.xixi.pojo.vo.admin;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 看板分布统计视图对象。
 */
@Data
public class DashboardDistributionVo {
    private Map<String, Integer> roleDistribution;
    private List<AuditStatVo> auditTypeDistribution;
}
