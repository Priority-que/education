package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.TeacherVerificationApplyDto;
import com.xixi.pojo.dto.TeacherVerificationAuditDto;
import com.xixi.pojo.query.TeacherVerificationHistoryQuery;
import com.xixi.pojo.vo.TeacherVerificationVo;
import com.xixi.web.Result;

public interface TeacherVerificationService {
    Result apply(TeacherVerificationApplyDto dto, Long userId);

    TeacherVerificationVo current(Long userId);

    IPage<TeacherVerificationVo> historyMyPage(TeacherVerificationHistoryQuery query, Long userId);

    IPage<TeacherVerificationVo> historyAdminPage(TeacherVerificationHistoryQuery query, Long userId, Integer userRole);

    IPage<TeacherVerificationVo> historyPage(TeacherVerificationHistoryQuery query, Long userId, Integer userRole);

    Result audit(Long applicationId, TeacherVerificationAuditDto dto, Long auditorId, Integer auditorRole);
}
