package com.xixi.pojo.dto.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发送沟通记录参数。
 */
@Data
public class CommunicationRecordSendDto {
    private Long applicationId;
    private Long jobId;
    private Long studentId;
    private String communicationType;
    private String communicationSubject;
    private String communicationContent;
    private String attachmentUrl;
    private Boolean needStudentConfirm;
    private LocalDateTime interviewTime;
    private String interviewAddress;
}
