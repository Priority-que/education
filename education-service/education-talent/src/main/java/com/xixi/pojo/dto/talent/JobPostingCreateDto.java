package com.xixi.pojo.dto.talent;

import lombok.Data;

import java.time.LocalDate;

/**
 * 创建岗位参数。
 */
@Data
public class JobPostingCreateDto {
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
}
