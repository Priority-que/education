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
 * 管理域事件生产者（事务提交后投递 MQ）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDomainEventProducer {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    @MethodPurpose("发布管理域事件，供事务提交后异步发送 MQ")
    public void publish(String eventType, String bizType, Long bizId, String payload, Long operatorId) {
        AdminDomainEvent event = new AdminDomainEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(eventType);
        event.setBizType(bizType);
        event.setBizId(bizId);
        event.setPayload(payload);
        event.setOperatorId(operatorId);
        event.setEventTime(LocalDateTime.now());
        applicationEventPublisher.publishEvent(event);
    }

    @MethodPurpose("事务提交后将管理域事件发送到 RabbitMQ")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterCommit(AdminDomainEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    AdminMqConstant.ADMIN_EVENT_EXCHANGE,
                    AdminMqConstant.ADMIN_EVENT_PUBLISH_ROUTING_KEY,
                    JSONUtil.toJsonStr(event)
            );
            log.info("admin event published, eventId={}, bizType={}, bizId={}",
                    event.getEventId(), event.getBizType(), event.getBizId());
        } catch (Exception e) {
            log.error("admin event publish failed, eventId={}, bizType={}, bizId={}",
                    event.getEventId(), event.getBizType(), event.getBizId(), e);
        }
    }
}
