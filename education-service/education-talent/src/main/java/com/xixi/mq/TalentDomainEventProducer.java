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
 * 人才域事件生产者（事务提交后投递 MQ）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TalentDomainEventProducer {
    public static final String EVENT_SEARCH_EXECUTED = "SEARCH_EXECUTED";
    public static final String EVENT_FAVORITE_CREATED = "FAVORITE_CREATED";
    public static final String EVENT_FAVORITE_UPDATED = "FAVORITE_UPDATED";
    public static final String EVENT_FAVORITE_STATUS_CHANGED = "FAVORITE_STATUS_CHANGED";
    public static final String EVENT_FAVORITE_DELETED = "FAVORITE_DELETED";
    public static final String EVENT_TAG_CHANGED = "TAG_CHANGED";
    public static final String EVENT_JOB_CHANGED = "JOB_CHANGED";
    public static final String EVENT_COMMUNICATION_SENT = "COMMUNICATION_SENT";
    public static final String EVENT_STAT_REBUILD = "STAT_REBUILD";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    @MethodPurpose("发布人才域事件，供事务提交后异步投递 MQ")
    public void publish(String eventType, Long enterpriseId, Long bizId, Object payload) {
        TalentDomainEvent event = new TalentDomainEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(eventType);
        event.setEnterpriseId(enterpriseId);
        event.setBizId(bizId);
        event.setPayload(payload == null ? null : JSONUtil.toJsonStr(payload));
        event.setEventTime(LocalDateTime.now());
        applicationEventPublisher.publishEvent(event);
    }

    @MethodPurpose("事务提交后发送人才域事件到 RabbitMQ")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void sendAfterCommit(TalentDomainEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    TalentMqConstant.TALENT_EVENT_EXCHANGE,
                    TalentMqConstant.TALENT_DOMAIN_EVENT_ROUTING_KEY,
                    JSONUtil.toJsonStr(event)
            );
            log.info("talent domain event published, eventId={}, type={}, enterpriseId={}, bizId={}",
                    event.getEventId(), event.getEventType(), event.getEnterpriseId(), event.getBizId());
        } catch (Exception e) {
            log.error("talent domain event publish failed, eventId={}, type={}",
                    event.getEventId(), event.getEventType(), e);
        }
    }
}
