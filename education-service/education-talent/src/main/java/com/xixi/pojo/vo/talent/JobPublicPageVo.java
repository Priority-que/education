package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生端岗位广场分页项视图对象。
 */
@Data
public class JobPublicPageVo {
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
    private String status;
    private Integer applyCount;
    private Integer viewCount;
    private Long enterpriseId;
    private String enterpriseName;
    private String enterpriseLogo;
    private Boolean enterpriseVerified;
    private Boolean studentApplied;
    private Long studentApplicationId;
    private Integer matchScore;
    private LocalDateTime publishTime;
}
