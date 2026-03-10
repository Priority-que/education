package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业端沟通分页项视图对象。
 */
@Data
public class TalentCommunicationPageVo {
    private Long recordId;
    private Long applicationId;
    private String applicationNo;
    private Long jobId;
    private String jobTitle;
    private Long studentId;
    private String studentName;
    private String communicationType;
    private String communicationSubject;
    private String communicationContent;
    private Boolean needStudentConfirm;
    private Boolean studentConfirmed;
    private Boolean isRead;
    private LocalDateTime readTime;
    private LocalDateTime createdTime;
}
