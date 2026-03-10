package com.xixi.service;

import com.xixi.pojo.dto.message.TemplateTriggerEventDto;
import com.xixi.web.Result;

/**
 * 模板触发服务。
 */
public interface TemplateTriggerService {
    Result triggerByEvent(TemplateTriggerEventDto dto, Long headerOperatorId, Integer headerOperatorRole);
}

