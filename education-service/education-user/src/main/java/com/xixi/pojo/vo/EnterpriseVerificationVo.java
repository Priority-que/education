package com.xixi.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业认证状态视图对象。
 */
@Data
public class EnterpriseVerificationVo {
    private Long applicationId;
    private Long enterpriseId;
    private String applicationNo;
    private String applyContent;
    private String status;
    private String auditReason;
    private LocalDateTime submittedTime;
    private LocalDateTime auditedTime;
    private Long auditorId;
}
