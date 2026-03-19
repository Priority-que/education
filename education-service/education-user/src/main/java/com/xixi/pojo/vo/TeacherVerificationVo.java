package com.xixi.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeacherVerificationVo {
    private Long applicationId;
    private Long teacherId;
    private String applicationNo;
    private String applyContent;
    private String status;
    private String auditReason;
    private LocalDateTime submittedTime;
    private LocalDateTime auditedTime;
    private Long auditorId;
}
