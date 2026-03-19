package com.xixi.pojo.dto.admin;

import lombok.Data;

/**
 * 审核拒绝请求参数。
 */
@Data
public class AuditRejectDto {
    private String auditOpinion;
    private String rejectReason;
}
