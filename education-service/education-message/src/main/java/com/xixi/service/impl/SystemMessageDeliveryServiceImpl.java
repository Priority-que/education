package com.xixi.service.impl;

import com.xixi.entity.SystemMessage;
import com.xixi.entity.UserMessage;
import com.xixi.constant.RoleConstants;
import com.xixi.mapper.MessageRecipientMapper;
import com.xixi.mapper.SystemMessageMapper;
import com.xixi.mapper.UserMessageMapper;
import com.xixi.service.MessageRecipientResolver;
import com.xixi.service.SystemMessageDeliveryService;
import com.xixi.service.TeacherMessageTargetService;
import com.xixi.support.TeacherIdentityResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 系统消息投递服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemMessageDeliveryServiceImpl implements SystemMessageDeliveryService {
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String RELATED_TYPE_SYSTEM_MESSAGE = "SYSTEM_MESSAGE";
    private static final String USER_MESSAGE_TYPE_SYSTEM = "SYSTEM";

    private final SystemMessageMapper systemMessageMapper;
    private final UserMessageMapper userMessageMapper;
    private final MessageRecipientResolver messageRecipientResolver;
    private final TeacherMessageTargetService teacherMessageTargetService;
    private final MessageRecipientMapper messageRecipientMapper;
    private final TeacherIdentityResolver teacherIdentityResolver;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deliver(Long systemMessageId) {
        if (systemMessageId == null) {
            return 0;
        }

        SystemMessage systemMessage = systemMessageMapper.selectById(systemMessageId);
        if (systemMessage == null) {
            log.warn("系统消息不存在，忽略投递, systemMessageId={}", systemMessageId);
            return 0;
        }
        if (!STATUS_PUBLISHED.equals(systemMessage.getStatus())) {
            log.info("系统消息状态非已发布，忽略投递, systemMessageId={}, status={}",
                    systemMessageId, systemMessage.getStatus());
            return 0;
        }

        List<Long> userIds = resolveRecipients(systemMessage);
        if (userIds.isEmpty()) {
            return 0;
        }

        Set<Long> deliveredUserSet = new HashSet<>(queryDeliveredUserIds(systemMessageId, userIds));
        List<UserMessage> insertList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Long userId : userIds) {
            if (deliveredUserSet.contains(userId)) {
                continue;
            }
            UserMessage userMessage = new UserMessage();
            userMessage.setUserId(userId);
            userMessage.setMessageType(USER_MESSAGE_TYPE_SYSTEM);
            userMessage.setMessageTitle(systemMessage.getMessageTitle());
            userMessage.setMessageContent(systemMessage.getMessageContent());
            userMessage.setRelatedId(systemMessageId);
            userMessage.setRelatedType(RELATED_TYPE_SYSTEM_MESSAGE);
            userMessage.setIsRead(false);
            userMessage.setPriority(systemMessage.getPriority());
            userMessage.setExpiryTime(systemMessage.getExpiryTime());
            userMessage.setCreatedTime(now);
            userMessage.setUpdatedTime(now);
            insertList.add(userMessage);
        }
        if (insertList.isEmpty()) {
            return 0;
        }
        return userMessageMapper.insertBatch(insertList);
    }

    private List<Long> queryDeliveredUserIds(Long systemMessageId, List<Long> userIds) {
        final int chunkSize = 500;
        List<Long> delivered = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, userIds.size());
            List<Long> chunk = userIds.subList(i, end);
            delivered.addAll(userMessageMapper.selectDeliveredUserIds(
                    systemMessageId, RELATED_TYPE_SYSTEM_MESSAGE, chunk));
        }
        return delivered;
    }

    private List<Long> resolveRecipients(SystemMessage systemMessage) {
        Integer senderRole = messageRecipientMapper.selectUserRoleById(systemMessage.getSenderId());
        if (senderRole != null && senderRole == RoleConstants.TEACHER) {
            Long teacherId = teacherIdentityResolver.resolveTeacherIdByUserId(systemMessage.getSenderId());
            return teacherMessageTargetService.resolveFromStored(
                    teacherId,
                    systemMessage.getTargetType(),
                    systemMessage.getTargetValue()
            ).recipientUserIds();
        }
        return messageRecipientResolver.resolveUserIds(systemMessage.getTargetType(), systemMessage.getTargetValue());
    }
}
