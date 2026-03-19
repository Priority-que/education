package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生端岗位详情视图对象。
 */
@Data
public class JobPublicDetailVo {
    private Long jobId;
    private String jobTitle;
    private String jobType;
    private String jobCategory;
    private String workLocation;
    private String salaryRange;
    private String educationRequirement;
    private String experienceRequirement;
    private Integer recruitmentNumber;
    private LocalDateTime applicationDeadline;
    private String contactEmail;
    private String contactPhone;
    private String jobDescription;
    private String requirements;
    private String benefits;
    private String status;
    private Integer applyCount;
    private Integer viewCount;
    private EnterpriseSnapshotVo enterpriseInfo;
    private Boolean studentApplied;
    private Long studentApplicationId;
    private List<JobPublicResumeOptionVo> recommendedResumeList;
    private MatchInfo matchInfo;

    @Data
    public static class MatchInfo {
        private Integer matchScore;
        private List<String> matchReasons;
        private List<String> missingRequirements;
    }
}
