package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.query.resume.PublicResumeQuery;
import com.xixi.pojo.vo.resume.PublicResumeDetailVo;
import com.xixi.pojo.vo.resume.PublicResumePageVo;
import com.xixi.pojo.vo.resume.PublicStudentResumePageVo;

public interface PublicResumeService {
    PublicResumeDetailVo getPublicResumeDetail(Long resumeId, Long viewerId, Integer viewerRole);

    IPage<PublicResumePageVo> getPublicResumePage(PublicResumeQuery query, Long viewerId);

    IPage<PublicStudentResumePageVo> getPublicStudentPage(PublicResumeQuery query, Long viewerId);

    default PublicResumeDetailVo getPublicStudentDetail(Long studentId, Long viewerId, Integer viewerRole) {
        return getPublicStudentDetail(studentId, null, viewerId, viewerRole);
    }

    PublicResumeDetailVo getPublicStudentDetail(Long studentId, Long resumeId, Long viewerId, Integer viewerRole);
}
