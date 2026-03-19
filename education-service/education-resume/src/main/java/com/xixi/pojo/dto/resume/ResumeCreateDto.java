package com.xixi.pojo.dto.resume;

import lombok.Data;

/**
 * 5.1 创建简历 DTO。
 */
@Data
public class ResumeCreateDto {
    private String resumeTitle;
    private String resumeTemplate;
    private String avatarUrl;
    private String contactEmail;
    private String contactPhone;
    private String careerObjective;
    private String selfIntroduction;
    private String visibility;
}
