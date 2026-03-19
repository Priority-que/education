package com.xixi.pojo.dto.admin;

import lombok.Data;

import java.util.List;

/**
 * 批量审核请求参数。
 */
@Data
public class AuditBatchHandleDto {
    private List<Long> auditIds;
    /**
     * APPROVE 或 REJECT。
     */
    private String action;
    private String auditOpinion;
    private String rejectReason;
}
