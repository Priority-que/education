package com.xixi.pojo.dto.resume;

import lombok.Data;

/**
 * 5.2 修改简历 DTO。
 */
@Data
public class ResumeUpdateDto {
    private Long id;
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
}
