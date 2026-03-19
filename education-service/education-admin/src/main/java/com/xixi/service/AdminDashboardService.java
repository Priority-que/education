package com.xixi.service;

import com.xixi.pojo.dto.admin.AdminInternalStatDailyDto;
import com.xixi.pojo.dto.admin.DashboardRebuildDto;
import com.xixi.pojo.query.admin.DashboardTrendQuery;
import com.xixi.pojo.vo.admin.DashboardDistributionVo;
import com.xixi.pojo.vo.admin.DashboardOverviewVo;
import com.xixi.pojo.vo.admin.DashboardTrendPointVo;
import com.xixi.web.Result;

import java.util.List;

/**
 * 管理端看板服务接口。
 */
public interface AdminDashboardService {
    DashboardOverviewVo getOverview();

    List<DashboardTrendPointVo> getTrend(DashboardTrendQuery query);

    DashboardDistributionVo getDistribution();

    Result rebuildStatistics(DashboardRebuildDto dto, Long adminId);

    Result reportDailyStatistics(AdminInternalStatDailyDto dto);
}
