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
 * 证书分享事件生产者（事务提交后投递 MQ）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateShareChangedEventProducer {
    public static final String EVENT_CREATE = "CREATE";
    public static final String EVENT_REVOKE = "REVOKE";
    public static final String EVENT_PUBLIC_VIEW = "PUBLIC_VIEW";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    @MethodPurpose("发布证书分享变更领域事件，供事务提交后异步投递 MQ")
    public void publish(String eventType, Long shareId, Long certificateId, Long studentId, String shareToken, Integer viewCount) {
        CertificateShareChangedEvent event = new CertificateShareChangedEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(eventType);
        event.setShareId(shareId);
        event.setCertificateId(certificateId);
        event.setStudentId(studentId);
        event.setShareToken(shareToken);
        event.setViewCount(viewCount);
        event.setEventTime(LocalDateTime.now());
        applicationEventPublisher.publishEvent(event);
    }

    @MethodPurpose("事务提交后将证书分享变更事件发送到 RabbitMQ")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterCommit(CertificateShareChangedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    CertificateMqConstant.CERTIFICATE_EVENT_EXCHANGE,
                    CertificateMqConstant.CERTIFICATE_SHARE_CHANGED_ROUTING_KEY,
                    JSONUtil.toJsonStr(event)
            );
            log.info("certificate share event published, eventId={}, type={}, shareId={}, certificateId={}",
                    event.getEventId(), event.getEventType(), event.getShareId(), event.getCertificateId());
        } catch (Exception e) {
            log.error("certificate share event publish failed, eventId={}, shareId={}",
                    event.getEventId(), event.getShareId(), e);
        }
    }
}

