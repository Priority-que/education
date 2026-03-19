package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.util.List;

/**
 * 简历快照视图对象。
 */
@Data
public class ResumeSnapshotVo {
    private Long resumeId;
    private String resumeTitle;
    private String visibility;
    private String careerObjective;
    private String skillSummary;
    private String contactEmail;
    private String contactPhone;
    private List<TalentApplicationCertificateVo> certificateList;
}
