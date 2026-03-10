package com.xixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.SystemMessage;
import com.xixi.exception.BizException;
import com.xixi.mapper.SystemMessageMapper;
import com.xixi.mq.SystemMessagePublishCommandProducer;
import com.xixi.pojo.dto.message.SystemMessageCreateDto;
import com.xixi.pojo.dto.message.SystemMessageUpdateDto;
import com.xixi.pojo.query.message.SystemMessageQuery;
import com.xixi.pojo.vo.message.SystemMessageDetailVo;
import com.xixi.service.MessageRecipientResolver;
import com.xixi.service.SystemMessageAdminService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 管理员系统消息服务实现。
 */
@Service
@RequiredArgsConstructor
public class SystemMessageAdminServiceImpl implements SystemMessageAdminService {
    private static final String TYPE_NOTICE = "NOTICE";
    private static final String TYPE_REMINDER = "REMINDER";
    private static final String TYPE_ANNOUNCEMENT = "ANNOUNCEMENT";

    private static final String TARGET_ALL = "ALL";
    private static final String TARGET_ROLE = "ROLE";
    private static final String TARGET_USER = "USER";

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_WITHDRAWN = "WITHDRAWN";

    private final SystemMessageMapper systemMessageMapper;
    private final MessageRecipientResolver messageRecipientResolver;
    private final SystemMessagePublishCommandProducer publishCommandProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("系统消息6.1：创建系统消息草稿")
    public Result createSystemMessage(SystemMessageCreateDto dto, Long operatorId) {
        validateCreateDto(dto);

        String normalizedTargetType = dto.getTargetType().trim().toUpperCase();
        String targetValueJson = messageRecipientResolver.normalizeTargetValue(normalizedTargetType, dto.getTargetValue());

        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setMessageType(dto.getMessageType().trim().toUpperCase());
        systemMessage.setMessageTitle(dto.getMessageTitle().trim());
        systemMessage.setMessageContent(dto.getMessageContent().trim());
        systemMessage.setSenderId(dto.getSenderId() == null ? operatorId : dto.getSenderId());
        systemMessage.setSenderName(StringUtils.hasText(dto.getSenderName()) ? dto.getSenderName().trim() : "系统管理员");
        systemMessage.setPriority(dto.getPriority() == null ? 0 : dto.getPriority());
        systemMessage.setTargetType(normalizedTargetType);
        systemMessage.setTargetValue(targetValueJson);
        systemMessage.setExpiryTime(dto.getExpiryTime());
        systemMessage.setStatus(STATUS_DRAFT);
        systemMessage.setCreatedTime(LocalDateTime.now());
        systemMessage.setUpdatedTime(LocalDateTime.now());
        systemMessageMapper.insert(systemMessage);

        return Result.success("创建系统消息成功", Map.of(
                "id", systemMessage.getId(),
                "status", systemMessage.getStatus()
        ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("系统消息6.2：修改系统消息草稿")
    public Result updateSystemMessage(SystemMessageUpdateDto dto, Long operatorId) {
        if (dto == null || dto.getId() == null) {
            throw new BizException(400, "系统消息ID不能为空");
        }

        SystemMessage existing = requireSystemMessage(dto.getId());
        String status = existing.getStatus();
        if (!STATUS_DRAFT.equals(status) && !STATUS_WITHDRAWN.equals(status)) {
            throw new BizException(409, "仅草稿或已撤回状态可修改");
        }

        boolean updated = false;
        if (StringUtils.hasText(dto.getMessageType())) {
            validateMessageType(dto.getMessageType());
            existing.setMessageType(dto.getMessageType().trim().toUpperCase());
            updated = true;
        }
        if (StringUtils.hasText(dto.getMessageTitle())) {
            existing.setMessageTitle(dto.getMessageTitle().trim());
            updated = true;
        }
        if (StringUtils.hasText(dto.getMessageContent())) {
            existing.setMessageContent(dto.getMessageContent().trim());
            updated = true;
        }
        if (dto.getSenderId() != null) {
            existing.setSenderId(dto.getSenderId());
            updated = true;
        } else if (existing.getSenderId() == null && operatorId != null) {
            existing.setSenderId(operatorId);
            updated = true;
        }
        if (dto.getSenderName() != null) {
            existing.setSenderName(trimToNull(dto.getSenderName()));
            updated = true;
        }
        if (dto.getPriority() != null) {
            validatePriority(dto.getPriority());
            existing.setPriority(dto.getPriority());
            updated = true;
        }
        if (dto.getExpiryTime() != null) {
            existing.setExpiryTime(dto.getExpiryTime());
            updated = true;
        }

        String finalTargetType = existing.getTargetType();
        List<Object> finalTargetValue = parseTargetValueAsList(existing.getTargetValue());
        if (StringUtils.hasText(dto.getTargetType())) {
            validateTargetType(dto.getTargetType());
            finalTargetType = dto.getTargetType().trim().toUpperCase();
            updated = true;
        }
        if (dto.getTargetValue() != null) {
            finalTargetValue = dto.getTargetValue();
            updated = true;
        }
        if (StringUtils.hasText(dto.getTargetType()) || dto.getTargetValue() != null) {
            existing.setTargetType(finalTargetType);
            existing.setTargetValue(messageRecipientResolver.normalizeTargetValue(finalTargetType, finalTargetValue));
        }

        if (!updated) {
            throw new BizException(400, "未检测到可更新字段");
        }
        existing.setUpdatedTime(LocalDateTime.now());
        systemMessageMapper.updateById(existing);
        return Result.success("修改系统消息成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("系统消息6.3：发布系统消息并在事务提交后发送MQ命令")
    public Result publishSystemMessage(Long id, Long operatorId) {
        SystemMessage systemMessage = requireSystemMessage(id);
        String status = systemMessage.getStatus();
        if (!STATUS_DRAFT.equals(status) && !STATUS_WITHDRAWN.equals(status)) {
            throw new BizException(409, "仅草稿或已撤回状态可发布");
        }

        LocalDateTime publishTime = LocalDateTime.now();
        systemMessage.setStatus(STATUS_PUBLISHED);
        systemMessage.setPublishTime(publishTime);
        systemMessage.setUpdatedTime(publishTime);
        systemMessageMapper.updateById(systemMessage);

        int recipientCount = messageRecipientResolver.countRecipients(
                systemMessage.getTargetType(), systemMessage.getTargetValue());
        publishCommandProducer.publish(systemMessage.getId(), operatorId, recipientCount, publishTime);

        return Result.success("发布成功", Map.of(
                "publishedCount", recipientCount,
                "publishTime", publishTime
        ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("系统消息6.4：撤回已发布系统消息")
    public Result withdrawSystemMessage(Long id, Long operatorId) {
        SystemMessage systemMessage = requireSystemMessage(id);
        if (!STATUS_PUBLISHED.equals(systemMessage.getStatus())) {
            throw new BizException(409, "仅已发布状态可撤回");
        }
        systemMessage.setStatus(STATUS_WITHDRAWN);
        systemMessage.setUpdatedTime(LocalDateTime.now());
        systemMessageMapper.updateById(systemMessage);
        return Result.success("撤回系统消息成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("系统消息6.5：删除草稿或已撤回消息")
    public Result deleteSystemMessage(Long id, Long operatorId) {
        SystemMessage systemMessage = requireSystemMessage(id);
        if (!STATUS_DRAFT.equals(systemMessage.getStatus()) && !STATUS_WITHDRAWN.equals(systemMessage.getStatus())) {
            throw new BizException(409, "仅草稿或已撤回状态可删除");
        }
        systemMessageMapper.deleteById(id);
        return Result.success("删除系统消息成功");
    }

    @Override
    @MethodPurpose("系统消息6.6：查询系统消息详情")
    public SystemMessageDetailVo getSystemMessageDetail(Long id) {
        return toVo(requireSystemMessage(id));
    }

    @Override
    @MethodPurpose("系统消息6.7：分页查询系统消息列表")
    public IPage<SystemMessageDetailVo> getSystemMessagePage(SystemMessageQuery query) {
        SystemMessageQuery safeQuery = query == null ? new SystemMessageQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 20 : safeQuery.getPageSize();

        LambdaQueryWrapper<SystemMessage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(safeQuery.getMessageType())) {
            wrapper.eq(SystemMessage::getMessageType, safeQuery.getMessageType().trim().toUpperCase());
        }
        if (StringUtils.hasText(safeQuery.getStatus())) {
            wrapper.eq(SystemMessage::getStatus, safeQuery.getStatus().trim().toUpperCase());
        }
        if (safeQuery.getPriority() != null) {
            wrapper.eq(SystemMessage::getPriority, safeQuery.getPriority());
        }
        if (StringUtils.hasText(safeQuery.getTargetType())) {
            wrapper.eq(SystemMessage::getTargetType, safeQuery.getTargetType().trim().toUpperCase());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(w -> w.like(SystemMessage::getMessageTitle, keyword)
                    .or()
                    .like(SystemMessage::getMessageContent, keyword));
        }
        if (safeQuery.getStartTime() != null) {
            wrapper.ge(SystemMessage::getCreatedTime, safeQuery.getStartTime());
        }
        if (safeQuery.getEndTime() != null) {
            wrapper.le(SystemMessage::getCreatedTime, safeQuery.getEndTime());
        }
        wrapper.orderByDesc(SystemMessage::getCreatedTime, SystemMessage::getUpdatedTime);

        Page<SystemMessage> entityPage = systemMessageMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<SystemMessageDetailVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVo).toList());
        return voPage;
    }

    @MethodPurpose("校验系统消息创建参数")
    private void validateCreateDto(SystemMessageCreateDto dto) {
        if (dto == null) {
            throw new BizException(400, "创建参数不能为空");
        }
        if (!StringUtils.hasText(dto.getMessageType())) {
            throw new BizException(400, "消息类型不能为空");
        }
        if (!StringUtils.hasText(dto.getMessageTitle())) {
            throw new BizException(400, "消息标题不能为空");
        }
        if (!StringUtils.hasText(dto.getMessageContent())) {
            throw new BizException(400, "消息内容不能为空");
        }
        if (!StringUtils.hasText(dto.getTargetType())) {
            throw new BizException(400, "目标类型不能为空");
        }
        validateMessageType(dto.getMessageType());
        validateTargetType(dto.getTargetType());
        validatePriority(dto.getPriority() == null ? 0 : dto.getPriority());
    }

    @MethodPurpose("校验系统消息类型")
    private void validateMessageType(String messageType) {
        String normalized = messageType.trim().toUpperCase();
        if (!Objects.equals(normalized, TYPE_NOTICE)
                && !Objects.equals(normalized, TYPE_REMINDER)
                && !Objects.equals(normalized, TYPE_ANNOUNCEMENT)) {
            throw new BizException(400, "消息类型非法，仅支持NOTICE/REMINDER/ANNOUNCEMENT");
        }
    }

    @MethodPurpose("校验消息优先级范围")
    private void validatePriority(Integer priority) {
        if (priority == null) {
            return;
        }
        if (priority < 0 || priority > 2) {
            throw new BizException(400, "优先级仅支持0/1/2");
        }
    }

    @MethodPurpose("校验消息目标类型")
    private void validateTargetType(String targetType) {
        String normalized = targetType.trim().toUpperCase();
        if (!Objects.equals(normalized, TARGET_ALL)
                && !Objects.equals(normalized, TARGET_ROLE)
                && !Objects.equals(normalized, TARGET_USER)) {
            throw new BizException(400, "目标类型非法，仅支持ALL/ROLE/USER");
        }
    }

    @MethodPurpose("按ID查询系统消息，不存在则抛出业务异常")
    private SystemMessage requireSystemMessage(Long id) {
        if (id == null) {
            throw new BizException(400, "系统消息ID不能为空");
        }
        SystemMessage systemMessage = systemMessageMapper.selectById(id);
        if (systemMessage == null) {
            throw new BizException(404, "系统消息不存在");
        }
        return systemMessage;
    }

    @MethodPurpose("系统消息实体转换为详情视图对象")
    private SystemMessageDetailVo toVo(SystemMessage entity) {
        SystemMessageDetailVo vo = new SystemMessageDetailVo();
        vo.setId(entity.getId());
        vo.setMessageType(entity.getMessageType());
        vo.setMessageTitle(entity.getMessageTitle());
        vo.setMessageContent(entity.getMessageContent());
        vo.setSenderId(entity.getSenderId());
        vo.setSenderName(entity.getSenderName());
        vo.setPriority(entity.getPriority());
        vo.setTargetType(entity.getTargetType());
        vo.setTargetValue(parseTargetValueAsList(entity.getTargetValue()));
        vo.setExpiryTime(entity.getExpiryTime());
        vo.setStatus(entity.getStatus());
        vo.setPublishTime(entity.getPublishTime());
        vo.setCreatedTime(entity.getCreatedTime());
        vo.setUpdatedTime(entity.getUpdatedTime());
        return vo;
    }

    @MethodPurpose("将目标值JSON转换为列表")
    private List<Object> parseTargetValueAsList(String targetValueJson) {
        if (!StringUtils.hasText(targetValueJson)) {
            return new ArrayList<>();
        }
        try {
            return JSONUtil.parseArray(targetValueJson).toList(Object.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @MethodPurpose("去除空白并转换为空值")
    private String trimToNull(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
    }
}
