package com.xixi.pojo.dto.admin;

import lombok.Data;

/**
 * 内部创建审核单请求参数。
 */
@Data
public class AdminInternalAuditCreateDto {
    private String auditType;
    private Long targetId;
    private String targetName;
    private Long applicantId;
    private String applicantName;
}
