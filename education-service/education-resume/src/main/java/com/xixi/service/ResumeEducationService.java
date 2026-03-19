package com.xixi.service;

import com.xixi.entity.ResumeEducation;
import com.xixi.pojo.dto.resume.ResumeEducationCreateDto;
import com.xixi.pojo.dto.resume.ResumeEducationUpdateDto;
import com.xixi.web.Result;

import java.util.List;

public interface ResumeEducationService {
    Result create(ResumeEducationCreateDto dto, Long studentId);

    Result update(ResumeEducationUpdateDto dto, Long studentId);

    Result delete(Long id, Long studentId);

    List<ResumeEducation> listByResumeId(Long resumeId, Long studentId);
}
