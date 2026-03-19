package com.xixi.pojo.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminInternalAuditSyncDto {
    private String auditType;
    private Long targetId;

    private String targetName;
    private Long applicantId;
    private String applicantName;

    private String auditStatus;
    private Long auditorId;
    private String auditorName;
    private String auditOpinion;
    private String rejectReason;
    private LocalDateTime auditTime;
}

