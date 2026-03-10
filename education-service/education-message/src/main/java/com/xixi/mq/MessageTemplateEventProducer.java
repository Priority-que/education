package com.xixi.mq;

import cn.hutool.json.JSONUtil;
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
 * 模板变更事件生产者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageTemplateEventProducer {
    private static final String EVENT_TYPE = "MESSAGE_TEMPLATE_CHANGED";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布模板变更事件（事务提交后发送MQ）。
     */
    public void publish(String action, Long operatorId, Long templateId, String templateCode) {
        MessageTemplateChangedEvent event = new MessageTemplateChangedEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(EVENT_TYPE);
        event.setAction(action);
        event.setOperatorId(operatorId);
        event.setTemplateId(templateId);
        event.setTemplateCode(templateCode);
        event.setOccurredAt(LocalDateTime.now());
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * 事务提交后将模板变更事件发送到RabbitMQ。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterCommit(MessageTemplateChangedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    MessageMqConstant.MESSAGE_COMMAND_EXCHANGE,
                    MessageMqConstant.TEMPLATE_CHANGE_ROUTING_KEY,
                    JSONUtil.toJsonStr(event)
            );
            log.info("发送模板变更事件成功, eventId={}, action={}, templateId={}",
                    event.getEventId(), event.getAction(), event.getTemplateId());
        } catch (Exception e) {
            log.error("发送模板变更事件失败, eventId={}, action={}, templateId={}",
                    event.getEventId(), event.getAction(), event.getTemplateId(), e);
        }
    }
}
