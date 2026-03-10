package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 企业端投递详情视图对象。
 */
@Data
public class TalentJobApplicationDetailVo {
    private Long applicationId;
    private String applicationNo;
    private String status;
    private String remark;
    private LocalDateTime submittedTime;
    private LocalDateTime updatedTime;
    private JobSnapshotVo jobInfo;
    private TalentApplicationStudentInfoVo studentInfo;
    private ResumeSnapshotVo resumeInfo;
    private List<TalentApplicationCertificateVo> certificateList;
    private List<JobApplicationCommunicationVo> communicationList;
    private List<JobApplicationTimelineVo> timeline;
}
