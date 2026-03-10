package com.xixi.pojo.dto.resume;

import lombok.Data;

import java.time.LocalDate;

/**
 * 项目经历修改参数。
 */
@Data
public class ResumeProjectUpdateDto {
    private Long id;
    private String projectName;
    private String projectRole;
    private LocalDate startDate;
    private LocalDate endDate;
    private String projectDescription;
    private String technologiesUsed;
    private String projectLink;
    private Integer sortOrder;
}
