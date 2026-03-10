package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.entity.AuditRecord;
import com.xixi.pojo.dto.admin.AuditApproveDto;
import com.xixi.pojo.dto.admin.AuditBatchHandleDto;
import com.xixi.pojo.dto.admin.AuditHandleDto;
import com.xixi.pojo.dto.admin.AuditRejectDto;
import com.xixi.pojo.query.admin.AuditPageQuery;
import com.xixi.pojo.vo.admin.AuditStatVo;
import com.xixi.web.Result;

import java.util.List;

/**
 * 审核中心服务接口。
 */
public interface AdminAuditService {
    IPage<AuditRecord> getAuditPage(AuditPageQuery query);

    AuditRecord getAuditDetail(Long auditId);

    Result approveAudit(Long auditId, AuditApproveDto dto, Long adminId);

    Result rejectAudit(Long auditId, AuditRejectDto dto, Long adminId);

    Result batchHandleAudit(AuditBatchHandleDto dto, Long adminId);

    List<AuditStatVo> getAuditStat(String auditType);

    Result handleAuditByScene(String requiredAuditType, Long auditId, AuditHandleDto dto, Long adminId);
}
