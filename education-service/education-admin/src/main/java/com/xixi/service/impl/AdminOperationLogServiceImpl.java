package com.xixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.OperationLog;
import com.xixi.exception.BizException;
import com.xixi.mapper.OperationLogMapper;
import com.xixi.mq.AdminDomainEventProducer;
import com.xixi.pojo.dto.admin.OperationLogReportDto;
import com.xixi.pojo.query.admin.OperationLogPageQuery;
import com.xixi.service.AdminOperationLogService;
import com.xixi.service.support.AdminOperationLogger;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 操作日志服务实现。
 */
@Service
@RequiredArgsConstructor
public class AdminOperationLogServiceImpl implements AdminOperationLogService {
    private static final String BIZ_TYPE_OPERATION_LOG = "OPERATION_LOG";

    private final OperationLogMapper operationLogMapper;
    private final AdminDomainEventProducer adminDomainEventProducer;
    private final AdminOperationLogger adminOperationLogger;

    @Override
    @MethodPurpose("分页查询操作日志")
    public IPage<OperationLog> getOperationLogPage(OperationLogPageQuery query) {
        OperationLogPageQuery safeQuery = query == null ? new OperationLogPageQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
        return operationLogMapper.selectOperationPage(
                new Page<>(pageNum, pageSize),
                safeQuery.getUserId(),
                normalizeUpper(safeQuery.getUserRole()),
                trimToNull(safeQuery.getOperationType()),
                safeQuery.getStatus(),
                safeQuery.getStartTime(),
                safeQuery.getEndTime()
        );
    }

    @Override
    @MethodPurpose("查询操作日志详情")
    public OperationLog getOperationLogDetail(Long id) {
        if (id == null) {
            throw new BizException(400, "id不能为空");
        }
        OperationLog log = operationLogMapper.selectById(id);
        if (log == null) {
            throw new BizException(404, "操作日志不存在");
        }
        return log;
    }

    @Override
    @MethodPurpose("导出操作日志数据")
    public List<OperationLog> exportOperationLog(OperationLogPageQuery query) {
        OperationLogPageQuery safeQuery = query == null ? new OperationLogPageQuery() : query;
        return operationLogMapper.selectForExport(
                safeQuery.getUserId(),
                normalizeUpper(safeQuery.getUserRole()),
                trimToNull(safeQuery.getOperationType()),
                safeQuery.getStatus(),
                safeQuery.getStartTime(),
                safeQuery.getEndTime(),
                5000
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("清理指定时间之前操作日志")
    public Result cleanupOperationLog(LocalDateTime beforeTime, Long adminId) {
        if (adminId == null) {
            throw new BizException(401, "未登录或管理员ID缺失");
        }
        if (beforeTime == null) {
            throw new BizException(400, "beforeTime不能为空");
        }
        int removed = operationLogMapper.deleteBeforeTime(beforeTime);
        adminDomainEventProducer.publish(
                "CLEANUP",
                BIZ_TYPE_OPERATION_LOG,
                null,
                JSONUtil.toJsonStr(Map.of("beforeTime", beforeTime.toString(), "removed", removed)),
                adminId
        );
        adminOperationLogger.log(
                adminId,
                "管理员" + adminId,
                "ADMIN",
                "OPERATION_LOG_CLEANUP",
                "清理操作日志",
                "DELETE",
                "/admin/log/operation/cleanup",
                JSONUtil.toJsonStr(Map.of("beforeTime", beforeTime.toString())),
                JSONUtil.toJsonStr(Map.of("removed", removed)),
                null,
                null,
                0,
                Boolean.TRUE,
                null
        );
        return Result.success("日志清理成功", Map.of("removed", removed));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("内部上报操作日志")
    public Result reportOperationLog(OperationLogReportDto dto) {
        if (dto == null || !StringUtils.hasText(dto.getOperationType())) {
            throw new BizException(400, "operationType不能为空");
        }
        OperationLog log = new OperationLog();
        log.setUserId(dto.getUserId());
        log.setUserName(trimToNull(dto.getUserName()));
        log.setUserRole(normalizeUpper(dto.getUserRole()));
        log.setOperationType(dto.getOperationType().trim());
        log.setOperationDescription(trimToNull(dto.getOperationDescription()));
        log.setRequestMethod(trimToNull(dto.getRequestMethod()));
        log.setRequestUrl(trimToNull(dto.getRequestUrl()));
        log.setRequestParams(trimToNull(dto.getRequestParams()));
        log.setResponseResult(trimToNull(dto.getResponseResult()));
        log.setIpAddress(trimToNull(dto.getIpAddress()));
        log.setUserAgent(trimToNull(dto.getUserAgent()));
        log.setExecuteTime(dto.getExecuteTime() == null ? 0 : dto.getExecuteTime());
        log.setStatus(dto.getStatus() == null ? Boolean.TRUE : dto.getStatus());
        log.setErrorMessage(trimToNull(dto.getErrorMessage()));
        log.setCreatedTime(LocalDateTime.now());
        operationLogMapper.insert(log);

        adminDomainEventProducer.publish(
                "REPORT",
                BIZ_TYPE_OPERATION_LOG,
                log.getId(),
                JSONUtil.toJsonStr(Map.of("operationType", log.getOperationType(), "status", log.getStatus())),
                null
        );
        return Result.success("操作日志上报成功");
    }

    @MethodPurpose("字符串去空并返回null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    @MethodPurpose("字符串标准化为大写")
    private String normalizeUpper(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }
}
