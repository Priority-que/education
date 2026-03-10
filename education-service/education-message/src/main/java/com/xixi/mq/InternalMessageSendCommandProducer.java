package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 内部消息投递命令生产者（事务提交后入队）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InternalMessageSendCommandProducer {
    private static final String DEFAULT_SOURCE_SERVICE = "education-message";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布内部投递命令。
     */
    @MethodPurpose("构建内部投递命令并发布事务事件")
    public InternalMessageSendCommand publish(String eventType, InternalMessageSendPayload payload) {
        InternalMessageSendCommand command = new InternalMessageSendCommand();
        command.setEventId(UUID.randomUUID().toString().replace("-", ""));
        command.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        command.setEventType(eventType);
        command.setSourceService(DEFAULT_SOURCE_SERVICE);
        command.setOccurredAt(LocalDateTime.now());
        command.setPayload(payload);
        applicationEventPublisher.publishEvent(command);
        return command;
    }

    /**
     * 事务提交后将命令发送到RabbitMQ。
     */
    @MethodPurpose("事务提交后将内部投递命令发送到RabbitMQ")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterCommit(InternalMessageSendCommand command) {
        String routingKey = toRoutingKey(command.getEventType());
        if (routingKey == null) {
            log.warn("未知内部投递事件类型，忽略发送，eventType={}, eventId={}",
                    command.getEventType(), command.getEventId());
            return;
        }
        try {
            rabbitTemplate.convertAndSend(
                    MessageMqConstant.MESSAGE_COMMAND_EXCHANGE,
                    routingKey,
                    JSONUtil.toJsonStr(command)
            );
            log.info("内部投递命令入队成功，eventId={}, eventType={}, routingKey={}",
                    command.getEventId(), command.getEventType(), routingKey);
        } catch (Exception e) {
            log.error("内部投递命令入队失败，eventId={}, eventType={}",
                    command.getEventId(), command.getEventType(), e);
        }
    }

    private String toRoutingKey(String eventType) {
        if (InternalMessageSendCommand.EVENT_SEND_USER.equals(eventType)) {
            return MessageMqConstant.SEND_USER_ROUTING_KEY;
        }
        if (InternalMessageSendCommand.EVENT_SEND_USERS.equals(eventType)) {
            return MessageMqConstant.SEND_USERS_ROUTING_KEY;
        }
        if (InternalMessageSendCommand.EVENT_SEND_ROLE.equals(eventType)) {
            return MessageMqConstant.SEND_ROLE_ROUTING_KEY;
        }
        if (InternalMessageSendCommand.EVENT_SEND_TEMPLATE.equals(eventType)) {
            return MessageMqConstant.SEND_TEMPLATE_ROUTING_KEY;
        }
        return null;
    }
}

