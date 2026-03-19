package com.xixi.pojo.dto;

import lombok.Data;

/**
 * 企业认证审核参数。
 */
@Data
public class EnterpriseVerificationAuditDto {
    private String status;
    private String auditReason;
    private Long auditorId;
}
