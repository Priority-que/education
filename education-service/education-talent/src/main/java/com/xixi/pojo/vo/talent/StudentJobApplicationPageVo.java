package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生端投递分页项视图对象。
 */
@Data
public class StudentJobApplicationPageVo {
    private Long applicationId;
    private String applicationNo;
    private Long jobId;
    private String jobTitle;
    private Long enterpriseId;
    private String enterpriseName;
    private Long resumeId;
    private String resumeTitle;
    private String status;
    private String statusText;
    private String latestCommunicationType;
    private LocalDateTime latestCommunicationTime;
    private Boolean hasUnreadUpdate;
    private LocalDateTime submittedTime;
    private LocalDateTime updatedTime;
}
