package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.SystemMonitor;
import com.xixi.pojo.query.admin.MonitorApiStatPageQuery;
import com.xixi.pojo.query.admin.MonitorAlertPageQuery;
import com.xixi.pojo.query.admin.MonitorSlowSqlPageQuery;
import com.xixi.pojo.query.admin.MonitorServicePageQuery;
import com.xixi.pojo.vo.admin.MonitorApiStatPageVo;
import com.xixi.pojo.vo.admin.MonitorSlowSqlPageVo;
import com.xixi.service.AdminMonitorService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统监控接口。
 */
@RestController
@RequestMapping("/admin/monitor")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ADMIN})
public class AdminMonitorController {
    private final AdminMonitorService adminMonitorService;

    @MethodPurpose("查询系统健康总览")
    @GetMapping("/health")
    public Result getHealth() {
        return Result.success(adminMonitorService.getHealthOverview());
    }

    @MethodPurpose("分页查询服务监控")
    @GetMapping("/service/page")
    public Result getServicePage(MonitorServicePageQuery query) {
        IPage<SystemMonitor> page = adminMonitorService.getServicePage(query);
        return Result.success(page);
    }

    @MethodPurpose("查询单服务监控明细")
    @GetMapping("/service/{serviceName}")
    public Result getServiceDetail(@PathVariable String serviceName) {
        List<SystemMonitor> list = adminMonitorService.getServiceDetail(serviceName);
        return Result.success(list);
    }

    @MethodPurpose("分页查询告警记录")
    @GetMapping("/alert/page")
    public Result getAlertPage(MonitorAlertPageQuery query) {
        IPage<SystemMonitor> page = adminMonitorService.getAlertPage(query);
        return Result.success(page);
    }

    @MethodPurpose("查询数据库监控总览")
    @GetMapping("/database/overview")
    public Result getDatabaseOverview() {
        return Result.success(adminMonitorService.getDatabaseOverview());
    }

    @MethodPurpose("分页查询慢SQL统计")
    @GetMapping("/database/slow-sql/page")
    public Result getSlowSqlPage(MonitorSlowSqlPageQuery query) {
        IPage<MonitorSlowSqlPageVo> page = adminMonitorService.getSlowSqlPage(query);
        return Result.success(page);
    }

    @MethodPurpose("查询API调用总览")
    @GetMapping("/api/overview")
    public Result getApiOverview() {
        return Result.success(adminMonitorService.getApiOverview());
    }

    @MethodPurpose("分页查询API调用统计")
    @GetMapping("/api/stat/page")
    public Result getApiStatPage(MonitorApiStatPageQuery query) {
        IPage<MonitorApiStatPageVo> page = adminMonitorService.getApiStatPage(query);
        return Result.success(page);
    }

    @MethodPurpose("查询API调用趋势")
    @GetMapping("/api/trend")
    public Result getApiTrend(MonitorApiStatPageQuery query) {
        return Result.success(adminMonitorService.getApiTrend(query));
    }
}
