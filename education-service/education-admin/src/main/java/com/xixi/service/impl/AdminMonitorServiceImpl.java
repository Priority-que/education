package com.xixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.SystemMonitor;
import com.xixi.exception.BizException;
import com.xixi.mapper.OperationLogMapper;
import com.xixi.mapper.SystemMonitorMapper;
import com.xixi.mq.AdminDomainEventProducer;
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
import com.xixi.service.AdminMonitorService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 系统监控服务实现。
 */
@Service
@RequiredArgsConstructor
public class AdminMonitorServiceImpl implements AdminMonitorService {
    private static final String BIZ_TYPE_MONITOR = "MONITOR";
    private static final int SLOW_SQL_THRESHOLD_MS = 800;

    private final SystemMonitorMapper systemMonitorMapper;
    private final OperationLogMapper operationLogMapper;
    private final AdminDomainEventProducer adminDomainEventProducer;

    @Override
    @MethodPurpose("查询系统健康总览")
    public MonitorHealthVo getHealthOverview() {
        MonitorHealthVo vo = systemMonitorMapper.selectHealthOverview();
        return vo == null ? new MonitorHealthVo() : vo;
    }

    @Override
    @MethodPurpose("分页查询服务监控最新状态")
    public IPage<SystemMonitor> getServicePage(MonitorServicePageQuery query) {
        MonitorServicePageQuery safeQuery = query == null ? new MonitorServicePageQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
        return systemMonitorMapper.selectServiceLatestPage(
                new Page<>(pageNum, pageSize),
                trimToNull(safeQuery.getServiceName()),
                normalizeUpper(safeQuery.getStatus())
        );
    }

    @Override
    @MethodPurpose("查询单服务监控明细")
    public List<SystemMonitor> getServiceDetail(String serviceName) {
        if (!StringUtils.hasText(serviceName)) {
            throw new BizException(400, "serviceName不能为空");
        }
        return systemMonitorMapper.selectServiceDetail(serviceName.trim(), 50);
    }

    @Override
    @MethodPurpose("分页查询告警记录")
    public IPage<SystemMonitor> getAlertPage(MonitorAlertPageQuery query) {
        MonitorAlertPageQuery safeQuery = query == null ? new MonitorAlertPageQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
        return systemMonitorMapper.selectAlertPage(
                new Page<>(pageNum, pageSize),
                safeQuery.getMinCpuUsage(),
                safeQuery.getMinMemoryUsage(),
                safeQuery.getMinErrorRate(),
                normalizeUpper(safeQuery.getStatus())
        );
    }

    @Override
    @MethodPurpose("查询数据库监控总览")
    public MonitorDatabaseOverviewVo getDatabaseOverview() {
        MonitorDatabaseOverviewVo vo = operationLogMapper.selectDatabaseOverview(SLOW_SQL_THRESHOLD_MS);
        if (vo == null) {
            vo = new MonitorDatabaseOverviewVo();
        }
        if (vo.getTotalConnections() == null) {
            vo.setTotalConnections(0);
        }
        if (vo.getActiveConnections() == null) {
            vo.setActiveConnections(0);
        }
        if (vo.getConnectionUsageRate() == null) {
            vo.setConnectionUsageRate(BigDecimal.ZERO);
        }
        if (vo.getSlowQueryCount() == null) {
            vo.setSlowQueryCount(0L);
        }
        if (vo.getAvgQueryTimeMs() == null) {
            vo.setAvgQueryTimeMs(BigDecimal.ZERO);
        }
        return vo;
    }

    @Override
    @MethodPurpose("分页查询慢SQL统计")
    public IPage<MonitorSlowSqlPageVo> getSlowSqlPage(MonitorSlowSqlPageQuery query) {
        MonitorSlowSqlPageQuery safeQuery = query == null ? new MonitorSlowSqlPageQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
        return operationLogMapper.selectSlowSqlPage(
                new Page<>(pageNum, pageSize),
                trimToNull(safeQuery.getKeyword()),
                SLOW_SQL_THRESHOLD_MS
        );
    }

    @Override
    @MethodPurpose("查询API调用总览")
    public MonitorApiOverviewVo getApiOverview() {
        LocalDateTime startTime = LocalDateTime.now().minusHours(24);
        MonitorApiOverviewVo vo = operationLogMapper.selectApiOverview(startTime);
        if (vo == null) {
            vo = new MonitorApiOverviewVo();
        }
        if (vo.getTotalCalls() == null) {
            vo.setTotalCalls(0L);
        }
        if (vo.getErrorCalls() == null) {
            vo.setErrorCalls(0L);
        }
        if (vo.getQps() == null) {
            vo.setQps(BigDecimal.ZERO);
        }
        if (vo.getAvgResponseTime() == null) {
            vo.setAvgResponseTime(BigDecimal.ZERO);
        }
        return vo;
    }

    @Override
    @MethodPurpose("分页查询API调用统计")
    public IPage<MonitorApiStatPageVo> getApiStatPage(MonitorApiStatPageQuery query) {
        MonitorApiStatPageQuery safeQuery = query == null ? new MonitorApiStatPageQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
        return operationLogMapper.selectApiStatPage(
                new Page<>(pageNum, pageSize),
                trimToNull(safeQuery.getKeyword())
        );
    }

    @Override
    @MethodPurpose("查询API调用趋势")
    public List<MonitorApiStatPageVo> getApiTrend(MonitorApiStatPageQuery query) {
        MonitorApiStatPageQuery safeQuery = query == null ? new MonitorApiStatPageQuery() : query;
        int limitSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0
                ? 8
                : Math.min(safeQuery.getPageSize().intValue(), 30);
        List<MonitorApiStatPageVo> list = operationLogMapper.selectApiTrend(limitSize);
        return list == null ? new ArrayList<>() : list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("内部上报系统监控数据")
    public Result reportMonitor(MonitorReportDto dto) {
        if (dto == null) {
            throw new BizException(400, "请求体不能为空");
        }
        if (!StringUtils.hasText(dto.getServerName()) || !StringUtils.hasText(dto.getServiceName())) {
            throw new BizException(400, "serverName和serviceName不能为空");
        }
        SystemMonitor monitor = new SystemMonitor();
        monitor.setServerName(dto.getServerName().trim());
        monitor.setServiceName(dto.getServiceName().trim());
        monitor.setCpuUsage(defaultDecimal(dto.getCpuUsage()));
        monitor.setMemoryUsage(defaultDecimal(dto.getMemoryUsage()));
        monitor.setDiskUsage(defaultDecimal(dto.getDiskUsage()));
        monitor.setHeapMemory(dto.getHeapMemory());
        monitor.setNonHeapMemory(dto.getNonHeapMemory());
        monitor.setThreadCount(dto.getThreadCount());
        monitor.setJvmUptime(dto.getJvmUptime());
        monitor.setGcCount(dto.getGcCount());
        monitor.setRequestCount(dto.getRequestCount());
        monitor.setErrorCount(dto.getErrorCount());
        monitor.setAvgResponseTime(defaultDecimal(dto.getAvgResponseTime()));
        monitor.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus().trim().toUpperCase() : "UP");
        monitor.setReportTime(dto.getReportTime() == null ? LocalDateTime.now() : dto.getReportTime());
        monitor.setCreatedTime(LocalDateTime.now());
        systemMonitorMapper.insert(monitor);

        adminDomainEventProducer.publish(
                "REPORT",
                BIZ_TYPE_MONITOR,
                monitor.getId(),
                JSONUtil.toJsonStr(Map.of(
                        "serviceName", monitor.getServiceName(),
                        "status", monitor.getStatus(),
                        "reportTime", monitor.getReportTime().toString()
                )),
                null
        );
        return Result.success("监控数据上报成功");
    }

    @MethodPurpose("缺省小数值")
    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    @MethodPurpose("字符串标准化为大写")
    private String normalizeUpper(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    @MethodPurpose("字符串去空并返回null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
