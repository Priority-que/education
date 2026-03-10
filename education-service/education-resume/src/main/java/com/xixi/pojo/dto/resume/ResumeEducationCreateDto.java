package com.xixi.pojo.dto.resume;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 教育经历新增参数。
 */
@Data
public class ResumeEducationCreateDto {
    private Long resumeId;
    private String schoolName;
    private String degree;
    private String major;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal gpa;
    private String ranking;
    private String honors;
    private String description;
    private Integer sortOrder;
}
