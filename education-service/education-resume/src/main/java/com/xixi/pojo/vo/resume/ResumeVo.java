package com.xixi.pojo.vo.resume;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历主档视图对象。
 */
@Data
public class ResumeVo {
    private Long id;
    private Long studentId;
    private String resumeTitle;
    private String resumeTemplate;
    private String avatarUrl;
    private String contactEmail;
    private String contactPhone;
    private String wechat;
    private String linkedin;
    private String github;
    private String selfIntroduction;
    private String careerObjective;
    private String visibility;
    private Integer viewCount;
    private Integer downloadCount;
    private Boolean isDefault;
    private Boolean status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
