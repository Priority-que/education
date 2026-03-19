package com.xixi.pojo.dto.resume;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 教育经历修改参数。
 */
@Data
public class ResumeEducationUpdateDto {
    private Long id;
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
