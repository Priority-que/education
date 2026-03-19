package com.xixi.pojo.vo.resume;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生维度公开候选分页项（每个学生仅展示一份主简历）。
 */
@Data
public class PublicStudentResumePageVo {
    private Long id;
    private Long studentId;
    private Long resumeId;
    private String resumeTitle;
    private String avatarUrl;
    private String careerObjective;
    private Integer viewCount;
    private String major;
    private String degree;
    private String skillSummary;
    private LocalDateTime updatedTime;
}
