package com.xixi.pojo.dto;

import lombok.Data;

@Data
public class TeacherVerificationAuditDto {
    private String status;
    private String auditReason;
    private Long auditorId;
}
