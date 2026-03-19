package com.xixi.pojo.dto.resume;

import lombok.Data;

import java.time.LocalDate;

/**
 * 工作经历新增参数。
 */
@Data
public class ResumeExperienceCreateDto {
    private Long resumeId;
    private String companyName;
    private String position;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
    private String location;
    private String description;
    private String achievements;
    private Integer sortOrder;
}
