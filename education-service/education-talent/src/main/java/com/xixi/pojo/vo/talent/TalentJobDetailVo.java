package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 企业端岗位详情视图对象。
 */
@Data
public class TalentJobDetailVo {
    private Long id;
    private Long enterpriseId;
    private String jobTitle;
    private String jobType;
    private String jobCategory;
    private String workLocation;
    private String salaryRange;
    private String experienceRequirement;
    private String educationRequirement;
    private String jobDescription;
    private String requirements;
    private String benefits;
    private Integer recruitmentNumber;
    private LocalDate applicationDeadline;
    private String contactEmail;
    private String contactPhone;
    private String status;
    private LocalDateTime publishTime;
    private Integer viewCount;
    private Integer applyCount;
    private Integer pendingCount;
    private Integer reviewingCount;
    private Integer interviewCount;
    private Integer offerCount;
    private Integer hiredCount;
    private Integer rejectedCount;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
