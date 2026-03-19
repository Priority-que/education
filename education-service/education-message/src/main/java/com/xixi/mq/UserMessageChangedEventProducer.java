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
import java.util.List;
import java.util.UUID;

/**
 * 我的消息变更事件生产者（事务提交后发布MQ）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserMessageChangedEventProducer {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布我的消息变更事件。
     */
    @MethodPurpose("发布我的消息已读/删除变更事件")
    public void publish(String action, Long userId, String messageType, List<Long> messageIds, Integer affectedCount) {
        UserMessageChangedEvent event = new UserMessageChangedEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(UserMessageChangedEvent.EVENT_TYPE);
        event.setAction(action);
        event.setUserId(userId);
        event.setMessageType(messageType);
        event.setMessageIds(messageIds);
        event.setAffectedCount(affectedCount);
        event.setOccurredAt(LocalDateTime.now());
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * 事务提交后发送我的消息变更事件到RabbitMQ。
     */
    @MethodPurpose("事务提交后发送我的消息变更事件到RabbitMQ")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterCommit(UserMessageChangedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    MessageMqConstant.MESSAGE_COMMAND_EXCHANGE,
                    MessageMqConstant.USER_MESSAGE_CHANGED_ROUTING_KEY,
                    JSONUtil.toJsonStr(event)
            );
            log.info("我的消息变更事件入队成功，eventId={}, action={}, userId={}, affectedCount={}",
                    event.getEventId(), event.getAction(), event.getUserId(), event.getAffectedCount());
        } catch (Exception e) {
            log.error("我的消息变更事件入队失败，eventId={}, action={}, userId={}",
                    event.getEventId(), event.getAction(), event.getUserId(), e);
        }
    }
}

