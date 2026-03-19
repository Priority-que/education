package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生端投递详情视图对象。
 */
@Data
public class StudentJobApplicationDetailVo {
    private Long applicationId;
    private String applicationNo;
    private String status;
    private LocalDateTime submittedTime;
    private LocalDateTime updatedTime;
    private JobSnapshotVo jobSnapshot;
    private EnterpriseSnapshotVo enterpriseSnapshot;
    private ResumeSnapshotVo resumeSnapshot;
    private List<JobApplicationTimelineVo> timeline;
    private List<JobApplicationCommunicationVo> communicationList;
    private Actions actions;

    @Data
    public static class Actions {
        private Boolean canWithdraw;
        private Boolean canConfirmInterview;
        private Boolean canUploadAttachment;
    }
}
