package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.entity.SystemMonitor;
import com.xixi.pojo.dto.admin.MonitorReportDto;
import com.xixi.pojo.query.admin.MonitorApiStatPageQuery;
import com.xixi.pojo.query.admin.MonitorAlertPageQuery;
import com.xixi.pojo.query.admin.MonitorSlowSqlPageQuery;
import com.xixi.pojo.query.admin.MonitorServicePageQuery;
import com.xixi.pojo.vo.admin.MonitorApiOverviewVo;
import com.xixi.pojo.vo.admin.MonitorApiStatPageVo;
import com.xixi.pojo.vo.admin.MonitorDatabaseOverviewVo;
import com.xixi.pojo.vo.admin.MonitorHealthVo;
import com.xixi.pojo.vo.admin.MonitorSlowSqlPageVo;
import com.xixi.web.Result;

import java.util.List;

/**
 * 系统监控服务接口。
 */
public interface AdminMonitorService {
    MonitorHealthVo getHealthOverview();

    IPage<SystemMonitor> getServicePage(MonitorServicePageQuery query);

    List<SystemMonitor> getServiceDetail(String serviceName);

    IPage<SystemMonitor> getAlertPage(MonitorAlertPageQuery query);

    MonitorDatabaseOverviewVo getDatabaseOverview();

    IPage<MonitorSlowSqlPageVo> getSlowSqlPage(MonitorSlowSqlPageQuery query);

    MonitorApiOverviewVo getApiOverview();

    IPage<MonitorApiStatPageVo> getApiStatPage(MonitorApiStatPageQuery query);

    List<MonitorApiStatPageVo> getApiTrend(MonitorApiStatPageQuery query);

    Result reportMonitor(MonitorReportDto dto);
}
