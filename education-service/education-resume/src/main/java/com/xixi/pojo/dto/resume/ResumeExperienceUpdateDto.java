package com.xixi.pojo.dto.resume;

import lombok.Data;

import java.time.LocalDate;

/**
 * 工作经历修改参数。
 */
@Data
public class ResumeExperienceUpdateDto {
    private Long id;
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
