package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生端沟通分页项视图对象。
 */
@Data
public class StudentCommunicationPageVo {
    private Long recordId;
    private Long applicationId;
    private String applicationNo;
    private Long jobId;
    private String jobTitle;
    private Long enterpriseId;
    private String enterpriseName;
    private String communicationType;
    private String communicationSubject;
    private String communicationContent;
    private Boolean isRead;
    private Boolean needStudentConfirm;
    private Boolean studentConfirmed;
    private LocalDateTime interviewTime;
    private String interviewAddress;
    private LocalDateTime createdTime;
}
