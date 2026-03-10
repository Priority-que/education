package com.xixi.pojo.vo.resume;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 6.2 公开简历分页项。
 */
@Data
public class PublicResumePageVo {
    private Long id;
    private String resumeTitle;
    private String avatarUrl;
    private String careerObjective;
    private Integer viewCount;
    private String major;
    private String degree;
    private String skillSummary;
    private LocalDateTime updatedTime;
}
