package com.xixi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.UserMessage;
import com.xixi.exception.BizException;
import com.xixi.mapper.UserMessageMapper;
import com.xixi.mq.UserMessageChangedEventProducer;
import com.xixi.pojo.dto.message.UserMessageBatchDeleteDto;
import com.xixi.pojo.dto.message.UserMessageBatchReadDto;
import com.xixi.pojo.query.message.UserMessageQuery;
import com.xixi.pojo.vo.message.UserMessageUnreadCountVo;
import com.xixi.pojo.vo.message.UserMessageVo;
import com.xixi.service.UserMessageCenterService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 我的消息中心服务实现。
 */
@Service
@RequiredArgsConstructor
public class UserMessageCenterServiceImpl implements UserMessageCenterService {
    private static final String MESSAGE_TYPE_SYSTEM = "SYSTEM";
    private static final String MESSAGE_TYPE_COURSE = "COURSE";
    private static final String MESSAGE_TYPE_CERTIFICATE = "CERTIFICATE";
    private static final String MESSAGE_TYPE_JOB = "JOB";
    private static final String MESSAGE_TYPE_OTHER = "OTHER";

    private static final String ACTION_READ_ONE = "READ_ONE";
    private static final String ACTION_READ_BATCH = "READ_BATCH";
    private static final String ACTION_READ_ALL = "READ_ALL";
    private static final String ACTION_DELETE_ONE = "DELETE_ONE";
    private static final String ACTION_DELETE_BATCH = "DELETE_BATCH";

    private final UserMessageMapper userMessageMapper;
    private final UserMessageChangedEventProducer userMessageChangedEventProducer;

    @Override
    @MethodPurpose("我的消息8.1：分页查询当前用户消息列表")
    public IPage<UserMessageVo> getMyMessagePage(Long userId, UserMessageQuery query) {
        Long currentUserId = requireUserId(userId);
        UserMessageQuery safeQuery = query == null ? new UserMessageQuery() : query;

        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 20 : safeQuery.getPageSize();
        Page<UserMessage> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<UserMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMessage::getUserId, currentUserId);
        if (StringUtils.hasText(safeQuery.getMessageType())) {
            wrapper.eq(UserMessage::getMessageType, normalizeMessageType(safeQuery.getMessageType()));
        }
        if (safeQuery.getIsRead() != null) {
            wrapper.eq(UserMessage::getIsRead, toReadBoolean(safeQuery.getIsRead()));
        }
        if (safeQuery.getPriority() != null) {
            wrapper.eq(UserMessage::getPriority, normalizePriority(safeQuery.getPriority()));
        }
        if (safeQuery.getStartTime() != null) {
            wrapper.ge(UserMessage::getCreatedTime, safeQuery.getStartTime());
        }
        if (safeQuery.getEndTime() != null) {
            wrapper.le(UserMessage::getCreatedTime, safeQuery.getEndTime());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(w -> w.like(UserMessage::getMessageTitle, keyword)
                    .or()
                    .like(UserMessage::getMessageContent, keyword));
        }
        wrapper.orderByDesc(UserMessage::getCreatedTime, UserMessage::getId);

        IPage<UserMessage> entityPage = userMessageMapper.selectPage(page, wrapper);
        Page<UserMessageVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVo).toList());
        return voPage;
    }

    @Override
    @MethodPurpose("我的消息8.2：查询当前用户消息详情（仅本人可见）")
    public UserMessageVo getMyMessageDetail(Long userId, Long messageId) {
        Long currentUserId = requireUserId(userId);
        UserMessage message = requireOwnMessage(currentUserId, messageId);
        return toVo(message);
    }

    @Override
    @MethodPurpose("我的消息8.3：统计当前用户未读消息数量")
    public UserMessageUnreadCountVo getMyUnreadCount(Long userId) {
        Long currentUserId = requireUserId(userId);
        UserMessageUnreadCountVo vo = new UserMessageUnreadCountVo();
        vo.setTotalUnread(countUnread(currentUserId, null));
        vo.setCourseUnread(countUnread(currentUserId, MESSAGE_TYPE_COURSE));
        vo.setCertificateUnread(countUnread(currentUserId, MESSAGE_TYPE_CERTIFICATE));
        vo.setJobUnread(countUnread(currentUserId, MESSAGE_TYPE_JOB));
        vo.setSystemUnread(countUnread(currentUserId, MESSAGE_TYPE_SYSTEM));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("我的消息8.4：标记单条消息为已读")
    public Result readMessage(Long userId, Long messageId) {
        Long currentUserId = requireUserId(userId);
        UserMessage message = requireOwnMessage(currentUserId, messageId);
        int affected = updateReadByIds(currentUserId, List.of(messageId));
        userMessageChangedEventProducer.publish(
                ACTION_READ_ONE,
                currentUserId,
                message.getMessageType(),
                List.of(messageId),
                affected
        );
        return Result.success("标记已读成功", Map.of("affectedCount", affected));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("我的消息8.5：批量标记消息为已读")
    public Result readMessageBatch(Long userId, UserMessageBatchReadDto dto) {
        Long currentUserId = requireUserId(userId);
        if (dto == null) {
            throw new BizException(400, "请求参数不能为空");
        }
        List<Long> messageIds = sanitizeMessageIds(dto.getMessageIds());
        int affected = updateReadByIds(currentUserId, messageIds);
        userMessageChangedEventProducer.publish(
                ACTION_READ_BATCH,
                currentUserId,
                null,
                messageIds,
                affected
        );
        return Result.success("批量标记已读成功", Map.of("affectedCount", affected));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("我的消息8.6：标记全部消息为已读（支持按类型过滤）")
    public Result readAllMessages(Long userId, String messageType) {
        Long currentUserId = requireUserId(userId);
        String normalizedType = StringUtils.hasText(messageType) ? normalizeMessageType(messageType) : null;

        UserMessage entity = new UserMessage();
        entity.setIsRead(true);
        entity.setReadTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        LambdaQueryWrapper<UserMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMessage::getUserId, currentUserId)
                .eq(UserMessage::getIsRead, false);
        if (normalizedType != null) {
            wrapper.eq(UserMessage::getMessageType, normalizedType);
        }

        int affected = userMessageMapper.update(entity, wrapper);
        userMessageChangedEventProducer.publish(
                ACTION_READ_ALL,
                currentUserId,
                normalizedType,
                null,
                affected
        );
        return Result.success("全部标记已读成功", Map.of("affectedCount", affected));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("我的消息8.7：删除单条消息")
    public Result deleteMessage(Long userId, Long messageId) {
        Long currentUserId = requireUserId(userId);
        UserMessage message = requireOwnMessage(currentUserId, messageId);
        int affected = userMessageMapper.delete(new LambdaQueryWrapper<UserMessage>()
                .eq(UserMessage::getId, messageId)
                .eq(UserMessage::getUserId, currentUserId));
        userMessageChangedEventProducer.publish(
                ACTION_DELETE_ONE,
                currentUserId,
                message.getMessageType(),
                List.of(messageId),
                affected
        );
        return Result.success("删除消息成功", Map.of("affectedCount", affected));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("我的消息8.8：批量删除消息")
    public Result deleteMessageBatch(Long userId, UserMessageBatchDeleteDto dto) {
        Long currentUserId = requireUserId(userId);
        if (dto == null) {
            throw new BizException(400, "请求参数不能为空");
        }
        List<Long> messageIds = sanitizeMessageIds(dto.getMessageIds());
        int affected = userMessageMapper.delete(new LambdaQueryWrapper<UserMessage>()
                .eq(UserMessage::getUserId, currentUserId)
                .in(UserMessage::getId, messageIds));
        userMessageChangedEventProducer.publish(
                ACTION_DELETE_BATCH,
                currentUserId,
                null,
                messageIds,
                affected
        );
        return Result.success("批量删除消息成功", Map.of("affectedCount", affected));
    }

    @MethodPurpose("校验并获取当前用户ID")
    private Long requireUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BizException(401, "未登录或用户身份缺失");
        }
        return userId;
    }

    @MethodPurpose("校验并获取当前用户可访问的消息记录")
    private UserMessage requireOwnMessage(Long userId, Long messageId) {
        if (messageId == null || messageId <= 0) {
            throw new BizException(400, "消息ID不能为空");
        }
        UserMessage message = userMessageMapper.selectById(messageId);
        if (message == null || !Objects.equals(message.getUserId(), userId)) {
            throw new BizException(404, "消息不存在");
        }
        return message;
    }

    @MethodPurpose("按消息ID列表更新当前用户消息为已读")
    private int updateReadByIds(Long userId, List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return 0;
        }
        UserMessage entity = new UserMessage();
        entity.setIsRead(true);
        entity.setReadTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        LambdaQueryWrapper<UserMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMessage::getUserId, userId)
                .in(UserMessage::getId, messageIds)
                .eq(UserMessage::getIsRead, false);
        return userMessageMapper.update(entity, wrapper);
    }

    @MethodPurpose("统计当前用户指定类型未读数量")
    private long countUnread(Long userId, String messageType) {
        LambdaQueryWrapper<UserMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMessage::getUserId, userId)
                .eq(UserMessage::getIsRead, false);
        if (StringUtils.hasText(messageType)) {
            wrapper.eq(UserMessage::getMessageType, messageType);
        }
        return userMessageMapper.selectCount(wrapper);
    }

    @MethodPurpose("清洗并校验批量消息ID列表")
    private List<Long> sanitizeMessageIds(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            throw new BizException(400, "messageIds不能为空");
        }
        LinkedHashSet<Long> idSet = new LinkedHashSet<>();
        for (Long messageId : messageIds) {
            if (messageId != null && messageId > 0) {
                idSet.add(messageId);
            }
        }
        if (idSet.isEmpty()) {
            throw new BizException(400, "messageIds不能为空");
        }
        return new ArrayList<>(idSet);
    }

    @MethodPurpose("规范化并校验消息类型")
    private String normalizeMessageType(String messageType) {
        if (!StringUtils.hasText(messageType)) {
            throw new BizException(400, "messageType不能为空");
        }
        String normalized = messageType.trim().toUpperCase();
        if (!MESSAGE_TYPE_SYSTEM.equals(normalized)
                && !MESSAGE_TYPE_COURSE.equals(normalized)
                && !MESSAGE_TYPE_CERTIFICATE.equals(normalized)
                && !MESSAGE_TYPE_JOB.equals(normalized)
                && !MESSAGE_TYPE_OTHER.equals(normalized)) {
            throw new BizException(400, "messageType仅支持SYSTEM/COURSE/CERTIFICATE/JOB/OTHER");
        }
        return normalized;
    }

    @MethodPurpose("校验并转换已读状态参数")
    private Boolean toReadBoolean(Integer isRead) {
        if (isRead == 0) {
            return false;
        }
        if (isRead == 1) {
            return true;
        }
        throw new BizException(400, "isRead仅支持0或1");
    }

    @MethodPurpose("校验优先级参数")
    private Integer normalizePriority(Integer priority) {
        if (priority < 0 || priority > 2) {
            throw new BizException(400, "priority仅支持0/1/2");
        }
        return priority;
    }

    @MethodPurpose("实体转消息视图对象")
    private UserMessageVo toVo(UserMessage entity) {
        UserMessageVo vo = new UserMessageVo();
        vo.setMessageId(entity.getId());
        vo.setMessageCategory(resolveMessageCategory(entity.getMessageType()));
        vo.setMessageTitle(entity.getMessageTitle());
        vo.setMessageContent(entity.getMessageContent());
        vo.setIsRead(Boolean.TRUE.equals(entity.getIsRead()) ? 1 : 0);
        vo.setReadStatus(Boolean.TRUE.equals(entity.getIsRead()) ? "已读" : "未读");
        vo.setReadTime(entity.getReadTime());
        vo.setPriority(entity.getPriority());
        vo.setPriorityText(resolvePriorityText(entity.getPriority()));
        vo.setExpiryTime(entity.getExpiryTime());
        vo.setCreatedTime(entity.getCreatedTime());
        return vo;
    }

    private String resolveMessageCategory(String messageType) {
        if (MESSAGE_TYPE_SYSTEM.equals(messageType)) {
            return "系统通知";
        }
        if (MESSAGE_TYPE_COURSE.equals(messageType)) {
            return "课程通知";
        }
        if (MESSAGE_TYPE_CERTIFICATE.equals(messageType)) {
            return "证书通知";
        }
        if (MESSAGE_TYPE_JOB.equals(messageType)) {
            return "就业通知";
        }
        return "其他通知";
    }

    private String resolvePriorityText(Integer priority) {
        if (priority == null || priority <= 0) {
            return "普通";
        }
        if (priority == 1) {
            return "重要";
        }
        return "紧急";
    }
}
