package com.xixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.constant.AdminBizConstants;
import com.xixi.entity.AuditRecord;
import com.xixi.exception.BizException;
import com.xixi.mapper.AuditRecordMapper;
import com.xixi.mq.AdminDomainEventProducer;
import com.xixi.pojo.dto.admin.AuditApproveDto;
import com.xixi.pojo.dto.admin.AuditBatchHandleDto;
import com.xixi.pojo.dto.admin.AuditHandleDto;
import com.xixi.pojo.dto.admin.AuditRejectDto;
import com.xixi.pojo.query.admin.AuditPageQuery;
import com.xixi.pojo.vo.admin.AuditStatRowVo;
import com.xixi.pojo.vo.admin.AuditStatVo;
import com.xixi.service.AdminAuditService;
import com.xixi.service.support.AdminOperationLogger;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 审核中心服务实现。
 */
@Service
@RequiredArgsConstructor
public class AdminAuditServiceImpl implements AdminAuditService {
    private static final String BIZ_TYPE_AUDIT = "AUDIT";
    private static final String EVENT_APPROVE = "APPROVE";
    private static final String EVENT_REJECT = "REJECT";
    private static final String EVENT_BATCH = "BATCH";

    private final AuditRecordMapper auditRecordMapper;
    private final AdminDomainEventProducer adminDomainEventProducer;
    private final AdminOperationLogger adminOperationLogger;
    private final ObjectProvider<AdminAuditService> adminAuditServiceProvider;

    @Override
    @MethodPurpose("分页查询审核记录")
    public IPage<AuditRecord> getAuditPage(AuditPageQuery query) {
        AuditPageQuery safeQuery = query == null ? new AuditPageQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
        return auditRecordMapper.selectAuditPage(
                new Page<>(pageNum, pageSize),
                normalizeUpper(safeQuery.getAuditType()),
                normalizeUpper(safeQuery.getAuditStatus()),
                trimToNull(safeQuery.getTargetName()),
                trimToNull(safeQuery.getApplicantName()),
                safeQuery.getStartTime(),
                safeQuery.getEndTime()
        );
    }

    @Override
    @MethodPurpose("查询审核记录详情")
    public AuditRecord getAuditDetail(Long auditId) {
        if (auditId == null) {
            throw new BizException(400, "auditId不能为空");
        }
        AuditRecord record = auditRecordMapper.selectById(auditId);
        if (record == null) {
            throw new BizException(404, "审核记录不存在");
        }
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("审核通过")
    public Result approveAudit(Long auditId, AuditApproveDto dto, Long adminId) {
        Long validAdminId = requireAdminId(adminId);
        String opinion = dto == null ? null : trimToNull(dto.getAuditOpinion());
        return handleAuditDecision(auditId, validAdminId, AdminBizConstants.AUDIT_STATUS_APPROVED, opinion, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("审核拒绝")
    public Result rejectAudit(Long auditId, AuditRejectDto dto, Long adminId) {
        Long validAdminId = requireAdminId(adminId);
        if (dto == null || !StringUtils.hasText(dto.getRejectReason())) {
            throw new BizException(400, "rejectReason不能为空");
        }
        String opinion = trimToNull(dto.getAuditOpinion());
        String rejectReason = dto.getRejectReason().trim();
        return handleAuditDecision(auditId, validAdminId, AdminBizConstants.AUDIT_STATUS_REJECTED, opinion, rejectReason);
    }

    @Override
    @MethodPurpose("批量处理审核记录，按单条事务隔离处理")
    public Result batchHandleAudit(AuditBatchHandleDto dto, Long adminId) {
        Long validAdminId = requireAdminId(adminId);
        List<Long> auditIds = normalizeIds(dto == null ? null : dto.getAuditIds());
        if (auditIds.isEmpty()) {
            throw new BizException(400, "auditIds不能为空");
        }
        if (auditIds.size() > 200) {
            throw new BizException(400, "单次批量审核最多200条");
        }
        String action = dto == null ? null : normalizeUpper(dto.getAction());
        if (!"APPROVE".equals(action) && !"REJECT".equals(action)) {
            throw new BizException(400, "action必须为APPROVE或REJECT");
        }
        if ("REJECT".equals(action) && (dto == null || !StringUtils.hasText(dto.getRejectReason()))) {
            throw new BizException(400, "批量拒绝时rejectReason不能为空");
        }

        List<Long> successIds = new ArrayList<>();
        List<Map<String, Object>> failedItems = new ArrayList<>();
        AdminAuditService proxy = adminAuditServiceProvider.getObject();
        for (Long auditId : auditIds) {
            try {
                if ("APPROVE".equals(action)) {
                    AuditApproveDto approveDto = new AuditApproveDto();
                    approveDto.setAuditOpinion(dto.getAuditOpinion());
                    proxy.approveAudit(auditId, approveDto, validAdminId);
                } else {
                    AuditRejectDto rejectDto = new AuditRejectDto();
                    rejectDto.setAuditOpinion(dto.getAuditOpinion());
                    rejectDto.setRejectReason(dto.getRejectReason());
                    proxy.rejectAudit(auditId, rejectDto, validAdminId);
                }
                successIds.add(auditId);
            } catch (BizException e) {
                failedItems.add(Map.of(
                        "auditId", auditId,
                        "code", e.getCode(),
                        "message", e.getMessage()
                ));
            } catch (Exception e) {
                failedItems.add(Map.of(
                        "auditId", auditId,
                        "code", 500,
                        "message", "系统异常：" + e.getMessage()
                ));
            }
        }

        adminDomainEventProducer.publish(
                EVENT_BATCH,
                BIZ_TYPE_AUDIT,
                null,
                JSONUtil.toJsonStr(Map.of(
                        "action", action,
                        "totalCount", auditIds.size(),
                        "successCount", successIds.size(),
                        "failedCount", failedItems.size()
                )),
                validAdminId
        );
        adminOperationLogger.log(
                validAdminId,
                "管理员" + validAdminId,
                "ADMIN",
                "AUDIT_BATCH",
                "批量审核",
                "POST",
                "/admin/audit/batch",
                JSONUtil.toJsonStr(dto),
                JSONUtil.toJsonStr(Map.of("successCount", successIds.size(), "failedCount", failedItems.size())),
                null,
                null,
                0,
                Boolean.TRUE,
                null
        );
        return Result.success("批量审核处理完成", Map.of(
                "totalCount", auditIds.size(),
                "successCount", successIds.size(),
                "failedCount", failedItems.size(),
                "successIds", successIds,
                "failedItems", failedItems
        ));
    }

    @Override
    @MethodPurpose("统计审核状态数据")
    public List<AuditStatVo> getAuditStat(String auditType) {
        List<AuditStatRowVo> rows = auditRecordMapper.selectAuditStatRows(normalizeUpper(auditType));
        List<AuditStatVo> result = new ArrayList<>();
        for (AuditStatRowVo row : rows) {
            AuditStatVo vo = new AuditStatVo();
            vo.setAuditType(row.getAuditType());
            vo.setTotalCount(nullToZero(row.getTotalCount()));
            vo.setPendingCount(nullToZero(row.getPendingCount()));
            vo.setApprovedCount(nullToZero(row.getApprovedCount()));
            vo.setRejectedCount(nullToZero(row.getRejectedCount()));
            vo.setApproveRate(calcRate(vo.getApprovedCount(), vo.getTotalCount()));
            vo.setRejectRate(calcRate(vo.getRejectedCount(), vo.getTotalCount()));
            result.add(vo);
        }
        return result;
    }

    @Override
    @MethodPurpose("按场景处理审核，支持课程/证书/资质审核入口")
    public Result handleAuditByScene(String requiredAuditType, Long auditId, AuditHandleDto dto, Long adminId) {
        if (dto == null || dto.getApproved() == null) {
            throw new BizException(400, "approved不能为空");
        }
        AuditRecord record = getAuditDetail(auditId);
        if (!requiredAuditType.equalsIgnoreCase(record.getAuditType())) {
            throw new BizException(409, "审核类型不匹配，当前记录类型为" + record.getAuditType());
        }
        if (Boolean.TRUE.equals(dto.getApproved())) {
            AuditApproveDto approveDto = new AuditApproveDto();
            approveDto.setAuditOpinion(dto.getAuditOpinion());
            return approveAudit(auditId, approveDto, adminId);
        }
        AuditRejectDto rejectDto = new AuditRejectDto();
        rejectDto.setAuditOpinion(dto.getAuditOpinion());
        rejectDto.setRejectReason(dto.getRejectReason());
        return rejectAudit(auditId, rejectDto, adminId);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @MethodPurpose("单条审核处理事务（供批量调用）")
    public Result handleAuditDecision(Long auditId, Long adminId, String status, String opinion, String rejectReason) {
        if (auditId == null) {
            throw new BizException(400, "auditId不能为空");
        }
        int affected = auditRecordMapper.updateAuditDecisionIfPending(
                auditId,
                adminId,
                "管理员" + adminId,
                status,
                opinion,
                rejectReason,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        if (affected <= 0) {
            AuditRecord record = auditRecordMapper.selectById(auditId);
            if (record == null) {
                throw new BizException(404, "审核记录不存在");
            }
            if (!AdminBizConstants.AUDIT_STATUS_PENDING.equalsIgnoreCase(record.getAuditStatus())) {
                throw new BizException(409, "审核记录已处理，当前状态：" + record.getAuditStatus());
            }
            throw new BizException(500, "审核更新失败");
        }

        adminDomainEventProducer.publish(
                AdminBizConstants.AUDIT_STATUS_APPROVED.equals(status) ? EVENT_APPROVE : EVENT_REJECT,
                BIZ_TYPE_AUDIT,
                auditId,
                JSONUtil.toJsonStr(Map.of(
                        "auditStatus", status,
                        "auditOpinion", opinion == null ? "" : opinion,
                        "rejectReason", rejectReason == null ? "" : rejectReason
                )),
                adminId
        );
        adminOperationLogger.log(
                adminId,
                "管理员" + adminId,
                "ADMIN",
                "AUDIT_" + status,
                "审核处理",
                "POST",
                "/admin/audit/" + ("APPROVED".equals(status) ? "approve/" : "reject/") + auditId,
                null,
                JSONUtil.toJsonStr(Map.of("auditId", auditId, "status", status)),
                null,
                null,
                0,
                Boolean.TRUE,
                null
        );
        return Result.success("审核处理成功", Map.of(
                "auditId", auditId,
                "auditStatus", status
        ));
    }

    @MethodPurpose("校验管理员ID")
    private Long requireAdminId(Long adminId) {
        if (adminId == null) {
            throw new BizException(401, "未登录或管理员ID缺失");
        }
        return adminId;
    }

    @MethodPurpose("字符串标准化为大写")
    private String normalizeUpper(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    @MethodPurpose("字符串去空并返回null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    @MethodPurpose("规范化并去重ID列表")
    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> set = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null && id > 0) {
                set.add(id);
            }
        }
        return new ArrayList<>(set);
    }

    @MethodPurpose("空值转0")
    private Integer nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    @MethodPurpose("计算比率")
    private BigDecimal calcRate(Integer part, Integer total) {
        if (total == null || total <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(part == null ? 0 : part)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}
