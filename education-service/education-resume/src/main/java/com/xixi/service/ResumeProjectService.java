package com.xixi.service;

import com.xixi.entity.ResumeProject;
import com.xixi.pojo.dto.resume.ResumeProjectCreateDto;
import com.xixi.pojo.dto.resume.ResumeProjectUpdateDto;
import com.xixi.web.Result;

import java.util.List;

public interface ResumeProjectService {
    Result create(ResumeProjectCreateDto dto, Long studentId);

    Result update(ResumeProjectUpdateDto dto, Long studentId);

    Result delete(Long id, Long studentId);

    List<ResumeProject> listByResumeId(Long resumeId, Long studentId);
}
