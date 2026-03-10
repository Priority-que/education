package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.message.TemplateTriggerRuleCreateDto;
import com.xixi.pojo.dto.message.TemplateTriggerRuleUpdateDto;
import com.xixi.pojo.query.message.TemplateTriggerRuleQuery;
import com.xixi.pojo.vo.message.TemplateTriggerRuleDetailVo;
import com.xixi.web.Result;

/**
 * 模板触发规则管理服务。
 */
public interface TemplateTriggerRuleAdminService {
    Result createRule(TemplateTriggerRuleCreateDto dto, Long operatorId);

    Result updateRule(TemplateTriggerRuleUpdateDto dto, Long operatorId);

    Result updateRuleStatus(Long id, Integer status, Long operatorId);

    Result deleteRule(Long id, Long operatorId);

    IPage<TemplateTriggerRuleDetailVo> getRulePage(TemplateTriggerRuleQuery query);
}

