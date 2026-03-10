package com.xixi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.MessageTemplateTriggerLog;
import com.xixi.entity.MessageTemplateTriggerRule;
import com.xixi.exception.BizException;
import com.xixi.mapper.MessageTemplateTriggerLogMapper;
import com.xixi.mapper.MessageTemplateTriggerRuleMapper;
import com.xixi.pojo.dto.message.InternalSendByTemplateDto;
import com.xixi.pojo.dto.message.TemplateTriggerEventDto;
import com.xixi.service.InternalMessageSendService;
import com.xixi.service.TemplateTriggerService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 模板触发服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateTriggerServiceImpl implements TemplateTriggerService {
    private static final String TARGET_ALL = "ALL";
    private static final String TARGET_ROLE = "ROLE";
    private static final String TARGET_USER = "USER";

    private static final String DELIVER_MODE_SYNC = "SYNC";
    private static final String DELIVER_MODE_MQ = "MQ";

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_SKIPPED = "SKIPPED";

    private static final String IDEMPOTENT_KEY_PREFIX = "message:template:trigger:";

    private final MessageTemplateTriggerRuleMapper triggerRuleMapper;
    private final MessageTemplateTriggerLogMapper triggerLogMapper;
    private final InternalMessageSendService internalMessageSendService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @MethodPurpose("内部触发：按事件编码匹配模板规则并自动发送消息")
    public Result triggerByEvent(TemplateTriggerEventDto dto, Long headerOperatorId, Integer headerOperatorRole) {
        validateEventDto(dto);
        String normalizedEventCode = dto.getEventCode().trim().toUpperCase();

        List<MessageTemplateTriggerRule> rules = triggerRuleMapper.selectList(
                new LambdaQueryWrapper<MessageTemplateTriggerRule>()
                        .eq(MessageTemplateTriggerRule::getEventCode, normalizedEventCode)
                        .eq(MessageTemplateTriggerRule::getStatus, true)
                        .orderByAsc(MessageTemplateTriggerRule::getId)
        );
        if (rules == null || rules.isEmpty()) {
            return Result.success("未匹配到启用触发规则", Map.of(
                    "eventId", dto.getEventId().trim(),
                    "eventCode", normalizedEventCode,
                    "matchedRules", 0
            ));
        }

        Long finalOperatorId = headerOperatorId != null ? headerOperatorId : dto.getOperatorId();
        Integer finalOperatorRole = headerOperatorRole != null ? headerOperatorRole : dto.getOperatorRole();
        int successCount = 0;
        int failedCount = 0;
        int skippedCount = 0;
        String eventId = dto.getEventId().trim();

        for (MessageTemplateTriggerRule rule : rules) {
            if (rule.getId() == null) {
                skippedCount++;
                continue;
            }
            Long ruleId = rule.getId();
            if (hasSuccessLog(eventId, ruleId)) {
                skippedCount++;
                safeInsertLog(eventId, normalizedEventCode, ruleId, rule.getTemplateCode(), STATUS_SKIPPED,
                        "事件规则已成功处理，跳过重复触发");
                continue;
            }

            String idempotentKey = IDEMPOTENT_KEY_PREFIX + eventId + ":" + ruleId;
            if (!tryAcquireIdempotentKey(idempotentKey)) {
                skippedCount++;
                safeInsertLog(eventId, normalizedEventCode, ruleId, rule.getTemplateCode(), STATUS_SKIPPED,
                        "事件规则正在处理或已处理，跳过重复触发");
                continue;
            }

            try {
                InternalSendByTemplateDto sendDto = buildTemplateSendDto(dto, rule);
                internalMessageSendService.sendByTemplate(sendDto, finalOperatorId, finalOperatorRole);
                successCount++;
                safeInsertLog(eventId, normalizedEventCode, ruleId, rule.getTemplateCode(), STATUS_SUCCESS, null);
            } catch (BizException e) {
                failedCount++;
                stringRedisTemplate.delete(idempotentKey);
                safeInsertLog(eventId, normalizedEventCode, ruleId, rule.getTemplateCode(), STATUS_FAILED, e.getMessage());
                log.warn("事件触发发送失败，eventId={}, eventCode={}, ruleId={}, message={}",
                        eventId, normalizedEventCode, ruleId, e.getMessage());
            } catch (Exception e) {
                failedCount++;
                stringRedisTemplate.delete(idempotentKey);
                safeInsertLog(eventId, normalizedEventCode, ruleId, rule.getTemplateCode(), STATUS_FAILED, e.getMessage());
                log.error("事件触发发送异常，eventId={}, eventCode={}, ruleId={}",
                        eventId, normalizedEventCode, ruleId, e);
            }
        }

        return Result.success("事件触发处理完成", Map.of(
                "eventId", eventId,
                "eventCode", normalizedEventCode,
                "matchedRules", rules.size(),
                "successCount", successCount,
                "failedCount", failedCount,
                "skippedCount", skippedCount
        ));
    }

    @MethodPurpose("构建按模板发送参数")
    private InternalSendByTemplateDto buildTemplateSendDto(TemplateTriggerEventDto event, MessageTemplateTriggerRule rule) {
        InternalSendByTemplateDto dto = new InternalSendByTemplateDto();
        dto.setTemplateCode(rule.getTemplateCode());
        dto.setTargetType(event.getTargetType().trim().toUpperCase());
        dto.setTargetValue(event.getTargetValue());
        dto.setParams(event.getParams());
        dto.setMessageType(firstNonBlank(rule.getMessageType(), event.getMessageType()));
        dto.setPriority(rule.getPriority() != null ? rule.getPriority() : event.getPriority());
        dto.setRelatedType(firstNonBlank(rule.getRelatedType(), event.getRelatedType()));
        dto.setRelatedId(event.getRelatedId());
        dto.setExpiryTime(event.getExpiryTime());
        dto.setDeliverMode(resolveDeliverMode(event.getDeliverMode(), rule.getDeliverMode()));
        return dto;
    }

    @MethodPurpose("校验事件参数")
    private void validateEventDto(TemplateTriggerEventDto dto) {
        if (dto == null) {
            throw new BizException(400, "触发事件参数不能为空");
        }
        if (!StringUtils.hasText(dto.getEventId())) {
            throw new BizException(400, "eventId不能为空");
        }
        if (!StringUtils.hasText(dto.getEventCode())) {
            throw new BizException(400, "eventCode不能为空");
        }
        String targetType = normalizeTargetType(dto.getTargetType());
        if ((Objects.equals(targetType, TARGET_ROLE) || Objects.equals(targetType, TARGET_USER))
                && (dto.getTargetValue() == null || dto.getTargetValue().isEmpty())) {
            throw new BizException(400, "targetValue不能为空");
        }
        if (StringUtils.hasText(dto.getDeliverMode())) {
            normalizeDeliverMode(dto.getDeliverMode());
        }
        if (dto.getPriority() != null && (dto.getPriority() < 0 || dto.getPriority() > 2)) {
            throw new BizException(400, "priority仅支持0/1/2");
        }
    }

    @MethodPurpose("校验并标准化目标类型")
    private String normalizeTargetType(String targetType) {
        if (!StringUtils.hasText(targetType)) {
            throw new BizException(400, "targetType不能为空");
        }
        String normalized = targetType.trim().toUpperCase();
        if (!Objects.equals(normalized, TARGET_ALL)
                && !Objects.equals(normalized, TARGET_ROLE)
                && !Objects.equals(normalized, TARGET_USER)) {
            throw new BizException(400, "targetType仅支持ALL/ROLE/USER");
        }
        return normalized;
    }

    @MethodPurpose("优先事件投递模式，否则回退规则投递模式")
    private String resolveDeliverMode(String eventDeliverMode, String ruleDeliverMode) {
        if (StringUtils.hasText(eventDeliverMode)) {
            return normalizeDeliverMode(eventDeliverMode);
        }
        if (StringUtils.hasText(ruleDeliverMode)) {
            return normalizeDeliverMode(ruleDeliverMode);
        }
        return DELIVER_MODE_MQ;
    }

    @MethodPurpose("校验并标准化投递模式")
    private String normalizeDeliverMode(String deliverMode) {
        String normalized = deliverMode.trim().toUpperCase();
        if (!Objects.equals(normalized, DELIVER_MODE_SYNC) && !Objects.equals(normalized, DELIVER_MODE_MQ)) {
            throw new BizException(400, "deliverMode仅支持SYNC/MQ");
        }
        return normalized;
    }

    @MethodPurpose("提取首个非空白字符串")
    private String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        if (StringUtils.hasText(second)) {
            return second.trim();
        }
        return null;
    }

    @MethodPurpose("尝试获取幂等锁")
    private boolean tryAcquireIdempotentKey(String key) {
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofDays(7));
        return !Boolean.FALSE.equals(success);
    }

    @MethodPurpose("判断事件规则是否已有成功记录")
    private boolean hasSuccessLog(String eventId, Long ruleId) {
        long successCount = triggerLogMapper.selectCount(new LambdaQueryWrapper<MessageTemplateTriggerLog>()
                .eq(MessageTemplateTriggerLog::getEventId, eventId)
                .eq(MessageTemplateTriggerLog::getRuleId, ruleId)
                .eq(MessageTemplateTriggerLog::getSendStatus, STATUS_SUCCESS));
        return successCount > 0;
    }

    @MethodPurpose("安全写入触发日志，日志异常不影响主流程")
    private void safeInsertLog(
            String eventId,
            String eventCode,
            Long ruleId,
            String templateCode,
            String sendStatus,
            String errorMessage
    ) {
        try {
            MessageTemplateTriggerLog logEntity = new MessageTemplateTriggerLog();
            logEntity.setEventId(eventId);
            logEntity.setEventCode(eventCode);
            logEntity.setRuleId(ruleId);
            logEntity.setTemplateCode(trimToNull(templateCode));
            logEntity.setSendStatus(sendStatus);
            logEntity.setErrorMessage(truncate(trimToNull(errorMessage), 500));
            logEntity.setCreatedTime(LocalDateTime.now());
            triggerLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.warn("写入模板触发日志失败，eventId={}, ruleId={}, sendStatus={}",
                    eventId, ruleId, sendStatus, e);
        }
    }

    @MethodPurpose("去除空白并转换为空值")
    private String trimToNull(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
    }

    @MethodPurpose("按最大长度截断文本")
    private String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}

