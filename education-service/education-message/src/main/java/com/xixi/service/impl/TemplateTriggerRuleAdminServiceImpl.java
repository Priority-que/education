package com.xixi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.MessageTemplate;
import com.xixi.entity.MessageTemplateTriggerRule;
import com.xixi.exception.BizException;
import com.xixi.mapper.MessageTemplateMapper;
import com.xixi.mapper.MessageTemplateTriggerRuleMapper;
import com.xixi.pojo.dto.message.TemplateTriggerRuleCreateDto;
import com.xixi.pojo.dto.message.TemplateTriggerRuleUpdateDto;
import com.xixi.pojo.query.message.TemplateTriggerRuleQuery;
import com.xixi.pojo.vo.message.TemplateTriggerRuleDetailVo;
import com.xixi.service.TemplateTriggerRuleAdminService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 模板触发规则管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class TemplateTriggerRuleAdminServiceImpl implements TemplateTriggerRuleAdminService {
    private static final String DELIVER_MODE_SYNC = "SYNC";
    private static final String DELIVER_MODE_MQ = "MQ";

    private static final String MESSAGE_TYPE_SYSTEM = "SYSTEM";
    private static final String MESSAGE_TYPE_COURSE = "COURSE";
    private static final String MESSAGE_TYPE_CERTIFICATE = "CERTIFICATE";
    private static final String MESSAGE_TYPE_JOB = "JOB";
    private static final String MESSAGE_TYPE_OTHER = "OTHER";

    private final MessageTemplateTriggerRuleMapper triggerRuleMapper;
    private final MessageTemplateMapper messageTemplateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("模板触发规则管理：创建触发规则")
    public Result createRule(TemplateTriggerRuleCreateDto dto, Long operatorId) {
        validateCreateDto(dto);
        String ruleCode = dto.getRuleCode().trim();
        long exists = triggerRuleMapper.selectCount(new LambdaQueryWrapper<MessageTemplateTriggerRule>()
                .eq(MessageTemplateTriggerRule::getRuleCode, ruleCode));
        if (exists > 0) {
            throw new BizException(409, "规则编码已存在");
        }

        String templateCode = dto.getTemplateCode().trim();
        requireTemplateExists(templateCode);

        MessageTemplateTriggerRule rule = new MessageTemplateTriggerRule();
        rule.setRuleCode(ruleCode);
        rule.setEventCode(dto.getEventCode().trim().toUpperCase());
        rule.setTemplateCode(templateCode);
        rule.setMessageType(normalizeMessageType(dto.getMessageType()));
        rule.setDeliverMode(normalizeDeliverMode(dto.getDeliverMode(), DELIVER_MODE_MQ));
        rule.setPriority(normalizePriority(dto.getPriority()));
        rule.setRelatedType(trimToNull(dto.getRelatedType()));
        rule.setStatus(toStatusBoolean(dto.getStatus(), true));
        rule.setRemark(trimToNull(dto.getRemark()));
        rule.setCreatedTime(LocalDateTime.now());
        rule.setUpdatedTime(LocalDateTime.now());
        triggerRuleMapper.insert(rule);

        return Result.success("创建触发规则成功", Map.of("id", rule.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("模板触发规则管理：更新触发规则")
    public Result updateRule(TemplateTriggerRuleUpdateDto dto, Long operatorId) {
        if (dto == null || dto.getId() == null) {
            throw new BizException(400, "规则ID不能为空");
        }
        MessageTemplateTriggerRule rule = requireRule(dto.getId());
        boolean updated = false;

        if (dto.getRuleCode() != null) {
            String ruleCode = requireText(dto.getRuleCode(), "规则编码不能为空");
            if (!Objects.equals(ruleCode, rule.getRuleCode())) {
                long exists = triggerRuleMapper.selectCount(new LambdaQueryWrapper<MessageTemplateTriggerRule>()
                        .eq(MessageTemplateTriggerRule::getRuleCode, ruleCode)
                        .ne(MessageTemplateTriggerRule::getId, rule.getId()));
                if (exists > 0) {
                    throw new BizException(409, "规则编码已存在");
                }
            }
            rule.setRuleCode(ruleCode);
            updated = true;
        }
        if (dto.getEventCode() != null) {
            rule.setEventCode(requireText(dto.getEventCode(), "事件编码不能为空").toUpperCase());
            updated = true;
        }
        if (dto.getTemplateCode() != null) {
            String templateCode = requireText(dto.getTemplateCode(), "模板编码不能为空");
            requireTemplateExists(templateCode);
            rule.setTemplateCode(templateCode);
            updated = true;
        }
        if (dto.getMessageType() != null) {
            rule.setMessageType(normalizeMessageType(dto.getMessageType()));
            updated = true;
        }
        if (dto.getDeliverMode() != null) {
            rule.setDeliverMode(normalizeDeliverMode(dto.getDeliverMode(), null));
            updated = true;
        }
        if (dto.getPriority() != null) {
            rule.setPriority(normalizePriority(dto.getPriority()));
            updated = true;
        }
        if (dto.getRelatedType() != null) {
            rule.setRelatedType(trimToNull(dto.getRelatedType()));
            updated = true;
        }
        if (dto.getStatus() != null) {
            rule.setStatus(toStatusBoolean(dto.getStatus(), null));
            updated = true;
        }
        if (dto.getRemark() != null) {
            rule.setRemark(trimToNull(dto.getRemark()));
            updated = true;
        }

        if (!updated) {
            throw new BizException(400, "未检测到可更新字段");
        }
        rule.setUpdatedTime(LocalDateTime.now());
        triggerRuleMapper.updateById(rule);
        return Result.success("更新触发规则成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("模板触发规则管理：更新规则状态")
    public Result updateRuleStatus(Long id, Integer status, Long operatorId) {
        MessageTemplateTriggerRule rule = requireRule(id);
        rule.setStatus(toStatusBoolean(status, null));
        rule.setUpdatedTime(LocalDateTime.now());
        triggerRuleMapper.updateById(rule);
        return Result.success("触发规则状态更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("模板触发规则管理：删除触发规则")
    public Result deleteRule(Long id, Long operatorId) {
        requireRule(id);
        triggerRuleMapper.deleteById(id);
        return Result.success("删除触发规则成功");
    }

    @Override
    @MethodPurpose("模板触发规则管理：分页查询触发规则")
    public IPage<TemplateTriggerRuleDetailVo> getRulePage(TemplateTriggerRuleQuery query) {
        TemplateTriggerRuleQuery safeQuery = query == null ? new TemplateTriggerRuleQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 20 : safeQuery.getPageSize();

        LambdaQueryWrapper<MessageTemplateTriggerRule> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(safeQuery.getRuleCode())) {
            wrapper.like(MessageTemplateTriggerRule::getRuleCode, safeQuery.getRuleCode().trim());
        }
        if (StringUtils.hasText(safeQuery.getEventCode())) {
            wrapper.eq(MessageTemplateTriggerRule::getEventCode, safeQuery.getEventCode().trim().toUpperCase());
        }
        if (StringUtils.hasText(safeQuery.getTemplateCode())) {
            wrapper.like(MessageTemplateTriggerRule::getTemplateCode, safeQuery.getTemplateCode().trim());
        }
        if (StringUtils.hasText(safeQuery.getDeliverMode())) {
            wrapper.eq(MessageTemplateTriggerRule::getDeliverMode, safeQuery.getDeliverMode().trim().toUpperCase());
        }
        if (safeQuery.getStatus() != null) {
            wrapper.eq(MessageTemplateTriggerRule::getStatus, toStatusBoolean(safeQuery.getStatus(), null));
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(w -> w.like(MessageTemplateTriggerRule::getRuleCode, keyword)
                    .or()
                    .like(MessageTemplateTriggerRule::getEventCode, keyword)
                    .or()
                    .like(MessageTemplateTriggerRule::getRemark, keyword));
        }
        wrapper.orderByDesc(MessageTemplateTriggerRule::getUpdatedTime, MessageTemplateTriggerRule::getCreatedTime);

        Page<MessageTemplateTriggerRule> entityPage = triggerRuleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<TemplateTriggerRuleDetailVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toDetailVo).toList());
        return voPage;
    }

    @MethodPurpose("校验创建参数")
    private void validateCreateDto(TemplateTriggerRuleCreateDto dto) {
        if (dto == null) {
            throw new BizException(400, "创建参数不能为空");
        }
        if (!StringUtils.hasText(dto.getRuleCode())) {
            throw new BizException(400, "规则编码不能为空");
        }
        if (!StringUtils.hasText(dto.getEventCode())) {
            throw new BizException(400, "事件编码不能为空");
        }
        if (!StringUtils.hasText(dto.getTemplateCode())) {
            throw new BizException(400, "模板编码不能为空");
        }
        if (dto.getDeliverMode() != null) {
            normalizeDeliverMode(dto.getDeliverMode(), null);
        }
        if (dto.getMessageType() != null) {
            normalizeMessageType(dto.getMessageType());
        }
        if (dto.getPriority() != null) {
            normalizePriority(dto.getPriority());
        }
        if (dto.getStatus() != null && dto.getStatus() != 0 && dto.getStatus() != 1) {
            throw new BizException(400, "规则状态只能为0或1");
        }
    }

    @MethodPurpose("按ID查询触发规则，不存在则抛出业务异常")
    private MessageTemplateTriggerRule requireRule(Long id) {
        if (id == null) {
            throw new BizException(400, "规则ID不能为空");
        }
        MessageTemplateTriggerRule rule = triggerRuleMapper.selectById(id);
        if (rule == null) {
            throw new BizException(404, "触发规则不存在");
        }
        return rule;
    }

    @MethodPurpose("校验模板编码是否存在")
    private void requireTemplateExists(String templateCode) {
        MessageTemplate template = messageTemplateMapper.selectOne(new LambdaQueryWrapper<MessageTemplate>()
                .eq(MessageTemplate::getTemplateCode, templateCode)
                .last("limit 1"));
        if (template == null) {
            throw new BizException(404, "模板不存在");
        }
    }

    @MethodPurpose("规则实体转换为详情视图对象")
    private TemplateTriggerRuleDetailVo toDetailVo(MessageTemplateTriggerRule entity) {
        TemplateTriggerRuleDetailVo vo = new TemplateTriggerRuleDetailVo();
        vo.setId(entity.getId());
        vo.setRuleCode(entity.getRuleCode());
        vo.setEventCode(entity.getEventCode());
        vo.setTemplateCode(entity.getTemplateCode());
        vo.setMessageType(entity.getMessageType());
        vo.setDeliverMode(entity.getDeliverMode());
        vo.setPriority(entity.getPriority());
        vo.setRelatedType(entity.getRelatedType());
        vo.setStatus(Boolean.TRUE.equals(entity.getStatus()) ? 1 : 0);
        vo.setRemark(entity.getRemark());
        vo.setCreatedTime(entity.getCreatedTime());
        vo.setUpdatedTime(entity.getUpdatedTime());
        return vo;
    }

    @MethodPurpose("校验并标准化投递模式")
    private String normalizeDeliverMode(String deliverMode, String defaultValue) {
        if (!StringUtils.hasText(deliverMode)) {
            if (defaultValue == null) {
                throw new BizException(400, "投递模式不能为空");
            }
            return defaultValue;
        }
        String normalized = deliverMode.trim().toUpperCase();
        if (!Objects.equals(normalized, DELIVER_MODE_SYNC) && !Objects.equals(normalized, DELIVER_MODE_MQ)) {
            throw new BizException(400, "投递模式仅支持SYNC/MQ");
        }
        return normalized;
    }

    @MethodPurpose("校验并标准化消息类型")
    private String normalizeMessageType(String messageType) {
        if (!StringUtils.hasText(messageType)) {
            return null;
        }
        String normalized = messageType.trim().toUpperCase();
        if (!Objects.equals(normalized, MESSAGE_TYPE_SYSTEM)
                && !Objects.equals(normalized, MESSAGE_TYPE_COURSE)
                && !Objects.equals(normalized, MESSAGE_TYPE_CERTIFICATE)
                && !Objects.equals(normalized, MESSAGE_TYPE_JOB)
                && !Objects.equals(normalized, MESSAGE_TYPE_OTHER)) {
            throw new BizException(400, "messageType仅支持SYSTEM/COURSE/CERTIFICATE/JOB/OTHER");
        }
        return normalized;
    }

    @MethodPurpose("校验优先级")
    private Integer normalizePriority(Integer priority) {
        if (priority == null) {
            return null;
        }
        if (priority < 0 || priority > 2) {
            throw new BizException(400, "priority仅支持0/1/2");
        }
        return priority;
    }

    @MethodPurpose("去除空白并转换为空值")
    private String trimToNull(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
    }

    @MethodPurpose("去除空白并返回文本")
    private String requireText(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new BizException(400, message);
        }
        return text.trim();
    }

    @MethodPurpose("将整型状态值转换为布尔状态")
    private Boolean toStatusBoolean(Integer status, Boolean defaultValue) {
        if (status == null) {
            if (defaultValue == null) {
                throw new BizException(400, "规则状态不能为空");
            }
            return defaultValue;
        }
        if (status == 1) {
            return true;
        }
        if (status == 0) {
            return false;
        }
        throw new BizException(400, "规则状态只能为0或1");
    }
}
