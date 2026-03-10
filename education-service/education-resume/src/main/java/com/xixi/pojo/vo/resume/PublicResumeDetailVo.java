package com.xixi.pojo.vo.resume;

import com.xixi.entity.ResumeEducation;
import com.xixi.entity.ResumeExperience;
import com.xixi.entity.ResumeProject;
import com.xixi.entity.ResumeSkill;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 6.1 公开简历详情。
 */
@Data
public class PublicResumeDetailVo {
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
    private LocalDateTime updatedTime;
    private List<ResumeEducation> educationList;
    private List<ResumeExperience> experienceList;
    private List<ResumeProject> projectList;
    private List<ResumeSkill> skillList;
    private List<PublicResumeCertificateVo> certificateList;
    private List<PublicResumeOptionVo> publicResumeOptions;
}
