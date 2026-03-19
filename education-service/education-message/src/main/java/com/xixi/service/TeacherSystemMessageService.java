package com.xixi.service;

import com.xixi.pojo.dto.message.SystemMessageCreateDto;
import com.xixi.pojo.dto.message.TeacherTargetPreviewDto;
import com.xixi.pojo.query.message.TeacherSystemMessageHistoryQuery;
import com.xixi.web.Result;

/**
 * 教师公告服务。
 */
public interface TeacherSystemMessageService {

    Result createTeacherSystemMessage(SystemMessageCreateDto dto, Long operatorId, Integer operatorRole);

    Result publishTeacherSystemMessage(Long id, Long operatorId, Integer operatorRole);

    Result previewTeacherTarget(TeacherTargetPreviewDto dto, Long operatorId, Integer operatorRole);

    Result searchTeacherReceivers(
            Long operatorId,
            Integer operatorRole,
            String keyword,
            Long courseId,
            Long classId,
            Integer pageNum,
            Integer pageSize
    );

    Result getTeacherSystemMessageHistoryPage(
            TeacherSystemMessageHistoryQuery query,
            Long operatorId,
            Integer operatorRole
    );
}
