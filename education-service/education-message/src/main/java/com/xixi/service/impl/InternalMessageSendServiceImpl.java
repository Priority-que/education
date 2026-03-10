package com.xixi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.MessageTemplate;
import com.xixi.entity.UserMessage;
import com.xixi.exception.BizException;
import com.xixi.mapper.MessageTemplateMapper;
import com.xixi.mapper.UserMessageMapper;
import com.xixi.mq.InternalMessageSendCommand;
import com.xixi.mq.InternalMessageSendCommandProducer;
import com.xixi.mq.InternalMessageSendPayload;
import com.xixi.mq.MessageMqConstant;
import com.xixi.pojo.dto.message.InternalSendByTemplateDto;
import com.xixi.pojo.dto.message.InternalSendToRoleDto;
import com.xixi.pojo.dto.message.InternalSendToUserDto;
import com.xixi.pojo.dto.message.InternalSendToUsersDto;
import com.xixi.service.InternalMessageSendService;
import com.xixi.service.MessageRecipientResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 内部消息投递服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InternalMessageSendServiceImpl implements InternalMessageSendService {
    private static final String DELIVER_MODE_SYNC = "SYNC";
    private static final String DELIVER_MODE_MQ = "MQ";

    private static final String TARGET_ALL = "ALL";
    private static final String TARGET_ROLE = "ROLE";
    private static final String TARGET_USER = "USER";

    private static final String MESSAGE_TYPE_SYSTEM = "SYSTEM";
    private static final String MESSAGE_TYPE_COURSE = "COURSE";
    private static final String MESSAGE_TYPE_CERTIFICATE = "CERTIFICATE";
    private static final String MESSAGE_TYPE_JOB = "JOB";
    private static final String MESSAGE_TYPE_OTHER = "OTHER";

    private static final int BATCH_SIZE = 500;
    private static final DateTimeFormatter DATE_FORMAT_CN = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT_CN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATETIME_SECONDS_FORMAT_CN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME_FORMAT_CN = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_SECONDS_FORMAT_CN = DateTimeFormatter.ofPattern("HH:mm");
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private final UserMessageMapper userMessageMapper;
    private final MessageTemplateMapper messageTemplateMapper;
    private final MessageRecipientResolver messageRecipientResolver;
    private final InternalMessageSendCommandProducer commandProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("内部投递9.1：发送给单个用户，支持同步或MQ异步模式")
    public Result sendToUser(InternalSendToUserDto dto, Long operatorId, Integer operatorRole) {
        if (dto == null || dto.getUserId() == null || dto.getUserId() <= 0) {
            throw new BizException(400, "userId不能为空");
        }
        InternalMessageSendPayload payload = buildDirectPayload(
                TARGET_USER,
                List.of((Object) dto.getUserId()),
                dto.getMessageType(),
                dto.getMessageTitle(),
                dto.getMessageContent(),
                dto.getRelatedId(),
                dto.getRelatedType(),
                dto.getPriority(),
                dto.getExpiryTime(),
                operatorId,
                operatorRole
        );
        return dispatch(dto.getDeliverMode(), InternalMessageSendCommand.EVENT_SEND_USER, payload);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("内部投递9.2：批量发送给用户列表，支持同步或MQ异步模式")
    public Result sendToUsers(InternalSendToUsersDto dto, Long operatorId, Integer operatorRole) {
        if (dto == null || dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
            throw new BizException(400, "userIds不能为空");
        }
        List<Object> userIds = dto.getUserIds().stream()
                .filter(Objects::nonNull)
                .map(value -> (Object) value)
                .toList();
        if (userIds.isEmpty()) {
            throw new BizException(400, "userIds不能为空");
        }
        InternalMessageSendPayload payload = buildDirectPayload(
                TARGET_USER,
                userIds,
                dto.getMessageType(),
                dto.getMessageTitle(),
                dto.getMessageContent(),
                dto.getRelatedId(),
                dto.getRelatedType(),
                dto.getPriority(),
                dto.getExpiryTime(),
                operatorId,
                operatorRole
        );
        return dispatch(dto.getDeliverMode(), InternalMessageSendCommand.EVENT_SEND_USERS, payload);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("内部投递9.3：按角色发送，支持同步或MQ异步模式")
    public Result sendToRole(InternalSendToRoleDto dto, Long operatorId, Integer operatorRole) {
        if (dto == null || dto.getRoleCodes() == null || dto.getRoleCodes().isEmpty()) {
            throw new BizException(400, "roleCodes不能为空");
        }
        InternalMessageSendPayload payload = buildDirectPayload(
                TARGET_ROLE,
                dto.getRoleCodes(),
                dto.getMessageType(),
                dto.getMessageTitle(),
                dto.getMessageContent(),
                dto.getRelatedId(),
                dto.getRelatedType(),
                dto.getPriority(),
                dto.getExpiryTime(),
                operatorId,
                operatorRole
        );
        return dispatch(dto.getDeliverMode(), InternalMessageSendCommand.EVENT_SEND_ROLE, payload);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("内部投递9.4：按模板渲染发送，支持同步或MQ异步模式")
    public Result sendByTemplate(InternalSendByTemplateDto dto, Long operatorId, Integer operatorRole) {
        if (dto == null || !StringUtils.hasText(dto.getTemplateCode())) {
            throw new BizException(400, "templateCode不能为空");
        }
        String normalizedTargetType = normalizeTargetType(dto.getTargetType());
        InternalMessageSendPayload payload = new InternalMessageSendPayload();
        payload.setTemplateCode(dto.getTemplateCode().trim());
        payload.setTargetType(normalizedTargetType);
        payload.setTargetValue(dto.getTargetValue());
        payload.setMessageType(normalizeMessageType(dto.getMessageType(), MESSAGE_TYPE_SYSTEM));
        payload.setMessageTitle(trimToNull(dto.getMessageTitle()));
        payload.setRelatedId(dto.getRelatedId());
        payload.setRelatedType(trimToNull(dto.getRelatedType()));
        payload.setPriority(normalizePriority(dto.getPriority()));
        payload.setExpiryTime(dto.getExpiryTime());
        payload.setParams(dto.getParams());
        payload.setOperatorId(operatorId);
        payload.setOperatorRole(operatorRole);
        return dispatch(dto.getDeliverMode(), InternalMessageSendCommand.EVENT_SEND_TEMPLATE, payload);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("异步消费者处理内部投递命令并执行落库")
    public int consumeAsyncCommand(InternalMessageSendCommand command) {
        if (command == null || command.getPayload() == null) {
            throw new BizException(400, "内部投递命令无效");
        }
        return switch (command.getEventType()) {
            case InternalMessageSendCommand.EVENT_SEND_USER,
                 InternalMessageSendCommand.EVENT_SEND_USERS,
                 InternalMessageSendCommand.EVENT_SEND_ROLE -> doSyncDirectSend(command.getPayload());
            case InternalMessageSendCommand.EVENT_SEND_TEMPLATE -> doSyncTemplateSend(command.getPayload());
            default -> {
                log.warn("忽略未知内部投递事件类型，eventType={}, eventId={}",
                        command.getEventType(), command.getEventId());
                yield 0;
            }
        };
    }

    private Result dispatch(String deliverMode, String eventType, InternalMessageSendPayload payload) {
        String normalizedMode = normalizeDeliverMode(deliverMode);
        if (DELIVER_MODE_MQ.equals(normalizedMode)) {
            InternalMessageSendCommand command = commandProducer.publish(eventType, payload);
            return Result.success("消息投递任务已入队", Map.of(
                    "eventId", command.getEventId(),
                    "queue", MessageMqConstant.INTERNAL_SEND_QUEUE
            ));
        }

        int successCount = switch (eventType) {
            case InternalMessageSendCommand.EVENT_SEND_USER,
                 InternalMessageSendCommand.EVENT_SEND_USERS,
                 InternalMessageSendCommand.EVENT_SEND_ROLE -> doSyncDirectSend(payload);
            case InternalMessageSendCommand.EVENT_SEND_TEMPLATE -> doSyncTemplateSend(payload);
            default -> throw new BizException(400, "不支持的内部投递事件类型");
        };
        return Result.success("消息发送成功", Map.of("successCount", successCount));
    }

    private InternalMessageSendPayload buildDirectPayload(
            String targetType,
            List<Object> targetValue,
            String messageType,
            String messageTitle,
            String messageContent,
            Long relatedId,
            String relatedType,
            Integer priority,
            LocalDateTime expiryTime,
            Long operatorId,
            Integer operatorRole
    ) {
        InternalMessageSendPayload payload = new InternalMessageSendPayload();
        payload.setTargetType(targetType);
        payload.setTargetValue(targetValue);
        payload.setMessageType(normalizeMessageType(messageType, null));
        payload.setMessageTitle(requireText(messageTitle, "messageTitle不能为空"));
        payload.setMessageContent(requireText(messageContent, "messageContent不能为空"));
        payload.setRelatedId(relatedId);
        payload.setRelatedType(trimToNull(relatedType));
        payload.setPriority(normalizePriority(priority));
        payload.setExpiryTime(expiryTime);
        payload.setOperatorId(operatorId);
        payload.setOperatorRole(operatorRole);
        return payload;
    }

    private int doSyncDirectSend(InternalMessageSendPayload payload) {
        String normalizedTargetType = normalizeTargetType(payload.getTargetType());
        String targetValueJson = messageRecipientResolver.normalizeTargetValue(normalizedTargetType, payload.getTargetValue());
        List<Long> userIds = messageRecipientResolver.resolveUserIds(normalizedTargetType, targetValueJson);
        if (userIds.isEmpty()) {
            return 0;
        }
        return insertUserMessages(
                userIds,
                normalizeMessageType(payload.getMessageType(), null),
                requireText(payload.getMessageTitle(), "messageTitle不能为空"),
                requireText(payload.getMessageContent(), "messageContent不能为空"),
                payload.getRelatedId(),
                trimToNull(payload.getRelatedType()),
                normalizePriority(payload.getPriority()),
                payload.getExpiryTime()
        );
    }

    private int doSyncTemplateSend(InternalMessageSendPayload payload) {
        String templateCode = requireText(payload.getTemplateCode(), "templateCode不能为空");
        MessageTemplate template = messageTemplateMapper.selectOne(new LambdaQueryWrapper<MessageTemplate>()
                .eq(MessageTemplate::getTemplateCode, templateCode)
                .last("limit 1"));
        if (template == null) {
            throw new BizException(404, "模板不存在");
        }
        if (!Boolean.TRUE.equals(template.getStatus())) {
            throw new BizException(409, "模板已禁用");
        }

        String normalizedTargetType = normalizeTargetType(payload.getTargetType());
        String targetValueJson = messageRecipientResolver.normalizeTargetValue(normalizedTargetType, payload.getTargetValue());
        List<Long> userIds = messageRecipientResolver.resolveUserIds(normalizedTargetType, targetValueJson);
        if (userIds.isEmpty()) {
            return 0;
        }

        String renderedSubject = renderTemplate(template.getTemplateSubject(), payload.getParams());
        String renderedContent = renderTemplate(template.getTemplateContent(), payload.getParams());
        String finalTitle = StringUtils.hasText(payload.getMessageTitle())
                ? payload.getMessageTitle().trim()
                : (StringUtils.hasText(renderedSubject) ? renderedSubject : template.getTemplateName());
        if (!StringUtils.hasText(finalTitle)) {
            finalTitle = "系统通知";
        }
        if (!StringUtils.hasText(renderedContent)) {
            throw new BizException(400, "模板渲染后内容为空");
        }

        return insertUserMessages(
                userIds,
                normalizeMessageType(payload.getMessageType(), MESSAGE_TYPE_SYSTEM),
                finalTitle,
                renderedContent,
                payload.getRelatedId(),
                trimToNull(payload.getRelatedType()),
                normalizePriority(payload.getPriority()),
                payload.getExpiryTime()
        );
    }

    private int insertUserMessages(
            List<Long> userIds,
            String messageType,
            String messageTitle,
            String messageContent,
            Long relatedId,
            String relatedType,
            Integer priority,
            LocalDateTime expiryTime
    ) {
        LocalDateTime now = LocalDateTime.now();
        List<UserMessage> messageList = new ArrayList<>(userIds.size());
        for (Long userId : userIds) {
            UserMessage userMessage = new UserMessage();
            userMessage.setUserId(userId);
            userMessage.setMessageType(messageType);
            userMessage.setMessageTitle(messageTitle);
            userMessage.setMessageContent(messageContent);
            userMessage.setRelatedId(relatedId);
            userMessage.setRelatedType(relatedType);
            userMessage.setIsRead(false);
            userMessage.setPriority(priority);
            userMessage.setExpiryTime(expiryTime);
            userMessage.setCreatedTime(now);
            userMessage.setUpdatedTime(now);
            messageList.add(userMessage);
        }
        return insertBatch(messageList);
    }

    private int insertBatch(List<UserMessage> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < list.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, list.size());
            List<UserMessage> chunk = list.subList(i, end);
            total += userMessageMapper.insertBatch(chunk);
        }
        return total;
    }

    private String normalizeDeliverMode(String deliverMode) {
        if (!StringUtils.hasText(deliverMode)) {
            return DELIVER_MODE_SYNC;
        }
        String normalized = deliverMode.trim().toUpperCase();
        if (!DELIVER_MODE_SYNC.equals(normalized) && !DELIVER_MODE_MQ.equals(normalized)) {
            throw new BizException(400, "deliverMode仅支持SYNC或MQ");
        }
        return normalized;
    }

    private String normalizeTargetType(String targetType) {
        if (!StringUtils.hasText(targetType)) {
            throw new BizException(400, "targetType不能为空");
        }
        String normalized = targetType.trim().toUpperCase();
        if (!TARGET_ALL.equals(normalized) && !TARGET_ROLE.equals(normalized) && !TARGET_USER.equals(normalized)) {
            throw new BizException(400, "targetType仅支持ALL/ROLE/USER");
        }
        return normalized;
    }

    private String normalizeMessageType(String messageType, String defaultValue) {
        String normalized = StringUtils.hasText(messageType) ? messageType.trim().toUpperCase() : defaultValue;
        if (!StringUtils.hasText(normalized)) {
            throw new BizException(400, "messageType不能为空");
        }
        if (!MESSAGE_TYPE_SYSTEM.equals(normalized)
                && !MESSAGE_TYPE_COURSE.equals(normalized)
                && !MESSAGE_TYPE_CERTIFICATE.equals(normalized)
                && !MESSAGE_TYPE_JOB.equals(normalized)
                && !MESSAGE_TYPE_OTHER.equals(normalized)) {
            throw new BizException(400, "messageType仅支持SYSTEM/COURSE/CERTIFICATE/JOB/OTHER");
        }
        return normalized;
    }

    private Integer normalizePriority(Integer priority) {
        int value = priority == null ? 0 : priority;
        if (value < 0 || value > 2) {
            throw new BizException(400, "priority仅支持0/1/2");
        }
        return value;
    }

    private String requireText(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new BizException(400, message);
        }
        return text.trim();
    }

    private String trimToNull(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
    }

    private String renderTemplate(String template, Map<String, Object> params) {
        if (!StringUtils.hasText(template)) {
            return "";
        }
        String rendered = template;
        if (params == null || params.isEmpty()) {
            return rendered;
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.hasText(key)) {
                continue;
            }
            String value = formatTemplateValue(entry.getValue());
            rendered = rendered.replace("{" + key.trim() + "}", value);
        }
        return rendered;
    }

    private String formatTemplateValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDateTime dateTime) {
            return formatLocalDateTime(dateTime);
        }
        if (value instanceof LocalDate date) {
            return DATE_FORMAT_CN.format(date);
        }
        if (value instanceof LocalTime time) {
            return formatLocalTime(time);
        }
        if (value instanceof ZonedDateTime zonedDateTime) {
            return formatLocalDateTime(zonedDateTime.toLocalDateTime());
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return formatLocalDateTime(offsetDateTime.toLocalDateTime());
        }
        if (value instanceof Instant instant) {
            return formatLocalDateTime(LocalDateTime.ofInstant(instant, DEFAULT_ZONE));
        }
        if (value instanceof Date date) {
            return formatLocalDateTime(LocalDateTime.ofInstant(date.toInstant(), DEFAULT_ZONE));
        }

        String text = String.valueOf(value);
        String formatted = tryFormatDateText(text);
        return formatted == null ? text : formatted;
    }

    private String tryFormatDateText(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String value = text.trim();
        if (!(value.contains("-") && (value.contains(":") || value.contains("T")))) {
            return null;
        }
        try {
            return formatLocalDateTime(LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (DateTimeParseException ignore) {
            // ignore
        }
        try {
            return formatLocalDateTime(LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (DateTimeParseException ignore) {
            // ignore
        }
        try {
            return formatLocalDateTime(LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        } catch (DateTimeParseException ignore) {
            // ignore
        }
        try {
            return DATE_FORMAT_CN.format(LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (DateTimeParseException ignore) {
            // ignore
        }
        return null;
    }

    private String formatLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        if (dateTime.getSecond() == 0 && dateTime.getNano() == 0) {
            return DATETIME_FORMAT_CN.format(dateTime);
        }
        return DATETIME_SECONDS_FORMAT_CN.format(dateTime);
    }

    private String formatLocalTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        if (time.getSecond() == 0 && time.getNano() == 0) {
            return TIME_FORMAT_CN.format(time);
        }
        return TIME_SECONDS_FORMAT_CN.format(time);
    }
}
