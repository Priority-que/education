package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.message.MessageTemplateCreateDto;
import com.xixi.pojo.dto.message.MessageTemplateUpdateDto;
import com.xixi.pojo.query.message.MessageTemplateQuery;
import com.xixi.pojo.vo.message.MessageTemplateDetailVo;
import com.xixi.web.Result;

public interface MessageTemplateAdminService {
    Result createTemplate(MessageTemplateCreateDto dto, Long operatorId);

    Result updateTemplate(MessageTemplateUpdateDto dto, Long operatorId);

    Result updateTemplateStatus(Long id, Integer status, Long operatorId);

    Result deleteTemplate(Long id, Long operatorId);

    MessageTemplateDetailVo getTemplateDetail(Long id);

    IPage<MessageTemplateDetailVo> getTemplatePage(MessageTemplateQuery query);
}
