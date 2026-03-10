package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.resume.ResumeViewLogRecordDto;
import com.xixi.pojo.vo.resume.ResumeViewLogStatVo;
import com.xixi.pojo.vo.resume.ResumeViewLogVo;
import com.xixi.web.Result;

public interface ResumeViewLogService {
    Result recordViewLog(ResumeViewLogRecordDto dto, Long operatorId, Integer operatorRole, String requestIp, String requestUserAgent);

    IPage<ResumeViewLogVo> getMyViewLogPage(Long resumeId, Long studentId, Long pageNum, Long pageSize);

    ResumeViewLogStatVo getMyViewStat(Long resumeId, Long studentId);
}
