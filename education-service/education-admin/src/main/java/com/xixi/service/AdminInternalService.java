package com.xixi.service;

import com.xixi.pojo.dto.admin.AdminInternalAuditCreateDto;
import com.xixi.pojo.dto.admin.AdminInternalAuditSyncDto;
import com.xixi.pojo.dto.admin.AdminInternalStatDailyDto;
import com.xixi.pojo.dto.admin.MonitorReportDto;
import com.xixi.pojo.dto.admin.OperationLogReportDto;
import com.xixi.web.Result;

/**
 * 管理服务内部接口服务。
 */
public interface AdminInternalService {
    Result createAudit(AdminInternalAuditCreateDto dto);

    Result syncAudit(AdminInternalAuditSyncDto dto);

    Result reportMonitor(MonitorReportDto dto);

    Result reportDailyStat(AdminInternalStatDailyDto dto);

    Result reportOperationLog(OperationLogReportDto dto);
}
