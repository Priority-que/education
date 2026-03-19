package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.entity.OperationLog;
import com.xixi.pojo.dto.admin.OperationLogReportDto;
import com.xixi.pojo.query.admin.OperationLogPageQuery;
import com.xixi.web.Result;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务接口。
 */
public interface AdminOperationLogService {
    IPage<OperationLog> getOperationLogPage(OperationLogPageQuery query);

    OperationLog getOperationLogDetail(Long id);

    List<OperationLog> exportOperationLog(OperationLogPageQuery query);

    Result cleanupOperationLog(LocalDateTime beforeTime, Long adminId);

    Result reportOperationLog(OperationLogReportDto dto);
}
