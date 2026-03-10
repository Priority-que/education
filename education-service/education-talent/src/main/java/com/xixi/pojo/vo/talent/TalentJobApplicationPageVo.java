package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业端投递分页项视图对象。
 */
@Data
public class TalentJobApplicationPageVo {
    private Long applicationId;
    private String applicationNo;
    private Long jobId;
    private String jobTitle;
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private Long resumeId;
    private String resumeTitle;
    private String status;
    private Integer matchScore;
    private Integer certificateVerifiedCount;
    private String latestCommunicationType;
    private LocalDateTime latestCommunicationTime;
    private LocalDateTime submittedTime;
    private LocalDateTime updatedTime;
}
