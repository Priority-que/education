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
 * 证书主档变更事件生产者（事务提交后投递 MQ）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateChangedEventProducer {
    public static final String EVENT_ISSUE = "ISSUE";
    public static final String EVENT_REVOKE = "REVOKE";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    @MethodPurpose("发布证书主档变更领域事件，供事务提交后异步投递 MQ")
    public void publish(String eventType, Long certificateId, String certificateNumber, Long studentId, Long teacherId, String status) {
        CertificateChangedEvent event = new CertificateChangedEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(eventType);
        event.setCertificateId(certificateId);
        event.setCertificateNumber(certificateNumber);
        event.setStudentId(studentId);
        event.setTeacherId(teacherId);
        event.setStatus(status);
        event.setEventTime(LocalDateTime.now());
        applicationEventPublisher.publishEvent(event);
    }

    @MethodPurpose("事务提交后将证书主档变更事件发送到 RabbitMQ")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterCommit(CertificateChangedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    CertificateMqConstant.CERTIFICATE_EVENT_EXCHANGE,
                    CertificateMqConstant.CERTIFICATE_CHANGED_ROUTING_KEY,
                    JSONUtil.toJsonStr(event)
            );
            log.info("certificate event published, eventId={}, type={}, certificateId={}, status={}",
                    event.getEventId(), event.getEventType(), event.getCertificateId(), event.getStatus());
        } catch (Exception e) {
            log.error("certificate event publish failed, eventId={}, certificateId={}",
                    event.getEventId(), event.getCertificateId(), e);
        }
    }
}

