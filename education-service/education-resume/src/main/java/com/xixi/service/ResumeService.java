package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.resume.ResumeCreateDto;
import com.xixi.pojo.dto.resume.ResumeUpdateDto;
import com.xixi.pojo.query.resume.ResumeQuery;
import com.xixi.pojo.vo.resume.ResumeVo;
import com.xixi.web.Result;

public interface ResumeService {
    Result createResume(ResumeCreateDto dto, Long studentId);

    Result updateResume(ResumeUpdateDto dto, Long studentId);

    Result deleteResume(Long resumeId, Long studentId);

    ResumeVo getResumeDetail(Long resumeId, Long studentId);

    IPage<ResumeVo> getMyResumePage(ResumeQuery query, Long studentId);

    Result setDefaultResume(Long resumeId, Long studentId);

    Result setVisibility(Long resumeId, String visibility, Long studentId);
}
