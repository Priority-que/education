package com.xixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import com.xixi.constant.AdminBizConstants;
import com.xixi.entity.AuditRecord;
import com.xixi.exception.BizException;
import com.xixi.mapper.AuditRecordMapper;
import com.xixi.mq.AdminDomainEventProducer;
import com.xixi.pojo.dto.admin.AdminInternalAuditCreateDto;
import com.xixi.pojo.dto.admin.AdminInternalAuditSyncDto;
import com.xixi.pojo.dto.admin.AdminInternalStatDailyDto;
import com.xixi.pojo.dto.admin.MonitorReportDto;
import com.xixi.pojo.dto.admin.OperationLogReportDto;
import com.xixi.service.AdminDashboardService;
import com.xixi.service.AdminInternalService;
import com.xixi.service.AdminMonitorService;
import com.xixi.service.AdminOperationLogService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminInternalServiceImpl implements AdminInternalService {
    private static final Set<String> VALID_STATUS = Set.of(
            AdminBizConstants.AUDIT_STATUS_PENDING,
            AdminBizConstants.AUDIT_STATUS_APPROVED,
            AdminBizConstants.AUDIT_STATUS_REJECTED
    );

    private final AuditRecordMapper auditRecordMapper;
    private final AdminMonitorService adminMonitorService;
    private final AdminDashboardService adminDashboardService;
    private final AdminOperationLogService adminOperationLogService;
    private final AdminDomainEventProducer adminDomainEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("内部创建审核单")
    public Result createAudit(AdminInternalAuditCreateDto dto) {
        validateAuditCreateDto(dto);

        String auditType = normalizeUpper(dto.getAuditType());
        AuditRecord existing = auditRecordMapper.selectLatestByTypeAndTargetId(auditType, dto.getTargetId());
        if (existing != null && AdminBizConstants.AUDIT_STATUS_PENDING.equalsIgnoreCase(existing.getAuditStatus())) {
            return Result.success("audit record already exists", Map.of("auditId", existing.getId()));
        }

        LocalDateTime now = LocalDateTime.now();
        AuditRecord record = new AuditRecord();
        record.setAuditType(auditType);
        record.setTargetId(dto.getTargetId());
        record.setTargetName(trimToNull(dto.getTargetName()));
        record.setApplicantId(dto.getApplicantId());
        record.setApplicantName(trimToNull(dto.getApplicantName()));
        record.setAuditStatus(AdminBizConstants.AUDIT_STATUS_PENDING);
        record.setCreatedTime(now);
        record.setUpdatedTime(now);
        auditRecordMapper.insert(record);

        adminDomainEventProducer.publish(
                "CREATE",
                "AUDIT",
                record.getId(),
                JSONUtil.toJsonStr(Map.of(
                        "auditType", record.getAuditType(),
                        "targetId", record.getTargetId()
                )),
                null
        );
        return Result.success("audit record created", Map.of("auditId", record.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("内部同步审核单状态")
    public Result syncAudit(AdminInternalAuditSyncDto dto) {
        validateAuditSyncDto(dto);

        String auditType = normalizeUpper(dto.getAuditType());
        String auditStatus = normalizeUpper(dto.getAuditStatus());
        LocalDateTime now = LocalDateTime.now();

        AuditRecord record = auditRecordMapper.selectLatestByTypeAndTargetId(auditType, dto.getTargetId());
        if (record == null) {
            record = new AuditRecord();
            record.setAuditType(auditType);
            record.setTargetId(dto.getTargetId());
            record.setTargetName(trimToNull(dto.getTargetName()));
            record.setApplicantId(dto.getApplicantId());
            record.setApplicantName(trimToNull(dto.getApplicantName()));
            record.setCreatedTime(now);
        } else {
            String targetName = trimToNull(dto.getTargetName());
            if (targetName != null) {
                record.setTargetName(targetName);
            }
            if (dto.getApplicantId() != null) {
                record.setApplicantId(dto.getApplicantId());
            }
            String applicantName = trimToNull(dto.getApplicantName());
            if (applicantName != null) {
                record.setApplicantName(applicantName);
            }
        }

        record.setAuditStatus(auditStatus);
        record.setAuditorId(dto.getAuditorId());
        record.setAuditorName(trimToNull(dto.getAuditorName()));
        record.setAuditOpinion(trimToNull(dto.getAuditOpinion()));
        record.setRejectReason(trimToNull(dto.getRejectReason()));
        if (AdminBizConstants.AUDIT_STATUS_PENDING.equals(auditStatus)) {
            record.setAuditTime(null);
        } else {
            record.setAuditTime(dto.getAuditTime() == null ? now : dto.getAuditTime());
        }
        record.setUpdatedTime(now);

        if (auditRecordMapper.selectById(record.getId()) == null) {
            auditRecordMapper.insert(record);
        } else {
            auditRecordMapper.updateById(record);
        }

        adminDomainEventProducer.publish(
                "SYNC",
                "AUDIT",
                record.getId(),
                JSONUtil.toJsonStr(Map.of(
                        "auditType", record.getAuditType(),
                        "targetId", record.getTargetId(),
                        "auditStatus", record.getAuditStatus()
                )),
                dto.getAuditorId()
        );

        return Result.success("audit record synced", Map.of("auditId", record.getId()));
    }

    @Override
    @MethodPurpose("内部上报监控数据")
    public Result reportMonitor(MonitorReportDto dto) {
        return adminMonitorService.reportMonitor(dto);
    }

    @Override
    @MethodPurpose("内部上报日统计快照")
    public Result reportDailyStat(AdminInternalStatDailyDto dto) {
        return adminDashboardService.reportDailyStatistics(dto);
    }

    @Override
    @MethodPurpose("内部上报操作日志")
    public Result reportOperationLog(OperationLogReportDto dto) {
        return adminOperationLogService.reportOperationLog(dto);
    }

    private void validateAuditCreateDto(AdminInternalAuditCreateDto dto) {
        if (dto == null) {
            throw new BizException(400, "request body cannot be null");
        }
        if (!StringUtils.hasText(dto.getAuditType())) {
            throw new BizException(400, "auditType cannot be null");
        }
        if (dto.getTargetId() == null || dto.getTargetId() <= 0) {
            throw new BizException(400, "targetId cannot be null");
        }
    }

    private void validateAuditSyncDto(AdminInternalAuditSyncDto dto) {
        if (dto == null) {
            throw new BizException(400, "request body cannot be null");
        }
        if (!StringUtils.hasText(dto.getAuditType())) {
            throw new BizException(400, "auditType cannot be null");
        }
        if (dto.getTargetId() == null || dto.getTargetId() <= 0) {
            throw new BizException(400, "targetId cannot be null");
        }
        String status = normalizeUpper(dto.getAuditStatus());
        if (!StringUtils.hasText(status) || !VALID_STATUS.contains(status)) {
            throw new BizException(400, "auditStatus is invalid");
        }
    }

    private String normalizeUpper(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
