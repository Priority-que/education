package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投递详情沟通记录视图对象。
 */
@Data
public class JobApplicationCommunicationVo {
    private Long recordId;
    private Long applicationId;
    private String applicationNo;
    private Long jobId;
    private String jobTitle;
    private Long enterpriseId;
    private String enterpriseName;
    private Long studentId;
    private String studentName;
    private String communicationType;
    private String communicationSubject;
    private String communicationContent;
    private String attachmentUrl;
    private Boolean needStudentConfirm;
    private Boolean studentConfirmed;
    private Boolean isRead;
    private LocalDateTime readTime;
    private LocalDateTime confirmTime;
    private String confirmRemark;
    private LocalDateTime interviewTime;
    private String interviewAddress;
    private LocalDateTime createdTime;
}
