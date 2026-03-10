package com.xixi.service;

import com.xixi.entity.ResumeSkill;
import com.xixi.pojo.dto.resume.ResumeSkillCreateDto;
import com.xixi.pojo.dto.resume.ResumeSkillUpdateDto;
import com.xixi.web.Result;

import java.util.List;

public interface ResumeSkillService {
    Result create(ResumeSkillCreateDto dto, Long studentId);

    Result update(ResumeSkillUpdateDto dto, Long studentId);

    Result delete(Long id, Long studentId);

    List<ResumeSkill> listByResumeId(Long resumeId, Long studentId);
}
