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
 * 简历变更事件生产者（事务提交后投递 MQ）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeChangedEventProducer {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布简历变更领域事件。
     */
    @MethodPurpose("发布简历主档变更事件，供事务提交后异步投递 MQ")
    public void publish(String eventType, Long resumeId, Long studentId, String visibility, Boolean isDefault) {
        ResumeChangedEvent event = new ResumeChangedEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(eventType);
        event.setResumeId(resumeId);
        event.setStudentId(studentId);
        event.setVisibility(visibility);
        event.setIsDefault(isDefault);
        event.setEventTime(LocalDateTime.now());
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * 事务提交后发送 MQ 消息。
     */
    @MethodPurpose("事务提交后将简历变更事件发送到 RabbitMQ")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterCommit(ResumeChangedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    ResumeMqConstant.RESUME_EVENT_EXCHANGE,
                    ResumeMqConstant.RESUME_CHANGED_ROUTING_KEY,
                    JSONUtil.toJsonStr(event)
            );
            log.info("resume event published, eventId={}, type={}, resumeId={}, studentId={}",
                    event.getEventId(), event.getEventType(), event.getResumeId(), event.getStudentId());
        } catch (Exception e) {
            log.error("resume event publish failed, eventId={}, resumeId={}",
                    event.getEventId(), event.getResumeId(), e);
        }
    }
}
