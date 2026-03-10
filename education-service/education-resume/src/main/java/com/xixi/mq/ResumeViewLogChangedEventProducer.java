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
 * 简历浏览日志变更事件生产者（事务提交后投递 MQ）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeViewLogChangedEventProducer {
    public static final String EVENT_RECORD = "RECORD";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布简历浏览日志变更事件。
     */
    @MethodPurpose("发布简历浏览日志变更事件，供事务提交后异步投递 MQ")
    public void publishRecord(Long viewLogId, Long resumeId, Long viewerId, String viewerType, LocalDateTime viewTime) {
        ResumeViewLogChangedEvent event = new ResumeViewLogChangedEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(EVENT_RECORD);
        event.setViewLogId(viewLogId);
        event.setResumeId(resumeId);
        event.setViewerId(viewerId);
        event.setViewerType(viewerType);
        event.setViewTime(viewTime);
        event.setEventTime(LocalDateTime.now());
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * 事务提交后发送 MQ 消息。
     */
    @MethodPurpose("事务提交后将简历浏览日志变更事件发送到 RabbitMQ")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterCommit(ResumeViewLogChangedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    ResumeMqConstant.RESUME_EVENT_EXCHANGE,
                    ResumeMqConstant.RESUME_VIEW_LOG_CHANGED_ROUTING_KEY,
                    JSONUtil.toJsonStr(event)
            );
            log.info("resume view log event published, eventId={}, type={}, viewLogId={}, resumeId={}",
                    event.getEventId(), event.getEventType(), event.getViewLogId(), event.getResumeId());
        } catch (Exception e) {
            log.error("resume view log event publish failed, eventId={}, viewLogId={}",
                    event.getEventId(), event.getViewLogId(), e);
        }
    }
}
