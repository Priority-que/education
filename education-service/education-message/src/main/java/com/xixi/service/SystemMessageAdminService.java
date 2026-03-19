package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.message.SystemMessageCreateDto;
import com.xixi.pojo.dto.message.SystemMessageUpdateDto;
import com.xixi.pojo.query.message.SystemMessageQuery;
import com.xixi.pojo.vo.message.SystemMessageDetailVo;
import com.xixi.web.Result;

public interface SystemMessageAdminService {
    Result createSystemMessage(SystemMessageCreateDto dto, Long operatorId);

    Result updateSystemMessage(SystemMessageUpdateDto dto, Long operatorId);

    Result publishSystemMessage(Long id, Long operatorId);

    Result withdrawSystemMessage(Long id, Long operatorId);

    Result deleteSystemMessage(Long id, Long operatorId);

    SystemMessageDetailVo getSystemMessageDetail(Long id);

    IPage<SystemMessageDetailVo> getSystemMessagePage(SystemMessageQuery query);
}
