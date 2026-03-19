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
 * 证书验证事件生产者（事务提交后投递 MQ）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateVerificationChangedEventProducer {
    public static final String EVENT_VERIFY = "VERIFY";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    @MethodPurpose("发布证书验证变更领域事件，供事务提交后异步投递 MQ")
    public void publish(
            Long verificationId,
            Long certificateId,
            String certificateNumber,
            String verificationMethod,
            String verificationResult,
            String verifierType
    ) {
        CertificateVerificationChangedEvent event = new CertificateVerificationChangedEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(EVENT_VERIFY);
        event.setVerificationId(verificationId);
        event.setCertificateId(certificateId);
        event.setCertificateNumber(certificateNumber);
        event.setVerificationMethod(verificationMethod);
        event.setVerificationResult(verificationResult);
        event.setVerifierType(verifierType);
        event.setEventTime(LocalDateTime.now());
        applicationEventPublisher.publishEvent(event);
    }

    @MethodPurpose("事务提交后将证书验证变更事件发送到 RabbitMQ")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterCommit(CertificateVerificationChangedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    CertificateMqConstant.CERTIFICATE_EVENT_EXCHANGE,
                    CertificateMqConstant.CERTIFICATE_VERIFICATION_CHANGED_ROUTING_KEY,
                    JSONUtil.toJsonStr(event)
            );
            log.info("certificate verification event published, eventId={}, verificationId={}, result={}",
                    event.getEventId(), event.getVerificationId(), event.getVerificationResult());
        } catch (Exception e) {
            log.error("certificate verification event publish failed, eventId={}, verificationId={}",
                    event.getEventId(), event.getVerificationId(), e);
        }
    }
}

