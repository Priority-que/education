package com.xixi.pojo.dto.resume;

import lombok.Data;

import java.time.LocalDate;

/**
 * 项目经历新增参数。
 */
@Data
public class ResumeProjectCreateDto {
    private Long resumeId;
    private String projectName;
    private String projectRole;
    private LocalDate startDate;
    private LocalDate endDate;
    private String projectDescription;
    private String technologiesUsed;
    private String projectLink;
    private Integer sortOrder;
}
