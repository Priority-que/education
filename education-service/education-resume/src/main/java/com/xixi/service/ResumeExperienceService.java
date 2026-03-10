package com.xixi.service;

import com.xixi.entity.ResumeExperience;
import com.xixi.pojo.dto.resume.ResumeExperienceCreateDto;
import com.xixi.pojo.dto.resume.ResumeExperienceUpdateDto;
import com.xixi.web.Result;

import java.util.List;

public interface ResumeExperienceService {
    Result create(ResumeExperienceCreateDto dto, Long studentId);

    Result update(ResumeExperienceUpdateDto dto, Long studentId);

    Result delete(Long id, Long studentId);

    List<ResumeExperience> listByResumeId(Long resumeId, Long studentId);
}
