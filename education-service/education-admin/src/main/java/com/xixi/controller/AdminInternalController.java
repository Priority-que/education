package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.pojo.dto.admin.AdminInternalAuditCreateDto;
import com.xixi.pojo.dto.admin.AdminInternalAuditSyncDto;
import com.xixi.pojo.dto.admin.AdminInternalStatDailyDto;
import com.xixi.pojo.dto.admin.MonitorReportDto;
import com.xixi.pojo.dto.admin.OperationLogReportDto;
import com.xixi.service.AdminInternalService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/internal")
@RequiredArgsConstructor
public class AdminInternalController {
    private final AdminInternalService adminInternalService;

    @MethodPurpose("内部创建审核单")
    @PostMapping("/audit/create")
    public Result createAudit(@RequestBody AdminInternalAuditCreateDto dto) {
        return adminInternalService.createAudit(dto);
    }

    @MethodPurpose("内部同步审核单状态")
    @PostMapping("/audit/sync")
    public Result syncAudit(@RequestBody AdminInternalAuditSyncDto dto) {
        return adminInternalService.syncAudit(dto);
    }

    @MethodPurpose("内部上报监控数据")
    @PostMapping("/monitor/report")
    public Result reportMonitor(@RequestBody MonitorReportDto dto) {
        return adminInternalService.reportMonitor(dto);
    }

    @MethodPurpose("内部上报统计快照")
    @PostMapping("/stat/daily")
    public Result reportDailyStat(@RequestBody AdminInternalStatDailyDto dto) {
        return adminInternalService.reportDailyStat(dto);
    }

    @MethodPurpose("内部上报操作日志")
    @PostMapping("/log/operation")
    public Result reportOperationLog(@RequestBody OperationLogReportDto dto) {
        return adminInternalService.reportOperationLog(dto);
    }
}
