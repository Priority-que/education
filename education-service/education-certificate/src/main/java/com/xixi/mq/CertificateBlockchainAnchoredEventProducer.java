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
 * 证书上链事件生产者（事务提交后投递 MQ）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateBlockchainAnchoredEventProducer {
    public static final String EVENT_ANCHOR = "ANCHOR";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    @MethodPurpose("发布证书上链完成事件，供事务提交后异步投递 MQ")
    public void publish(
            Long certificateId,
            String certificateNumber,
            Long blockHeight,
            String transactionHash,
            String currentHash,
            Long operatorId
    ) {
        CertificateBlockchainAnchoredEvent event = new CertificateBlockchainAnchoredEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(EVENT_ANCHOR);
        event.setCertificateId(certificateId);
        event.setCertificateNumber(certificateNumber);
        event.setBlockHeight(blockHeight);
        event.setTransactionHash(transactionHash);
        event.setCurrentHash(currentHash);
        event.setOperatorId(operatorId);
        event.setEventTime(LocalDateTime.now());
        applicationEventPublisher.publishEvent(event);
    }

    @MethodPurpose("事务提交后将证书上链事件发送到 RabbitMQ")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterCommit(CertificateBlockchainAnchoredEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    CertificateMqConstant.CERTIFICATE_EVENT_EXCHANGE,
                    CertificateMqConstant.CERTIFICATE_BLOCKCHAIN_ANCHORED_ROUTING_KEY,
                    JSONUtil.toJsonStr(event)
            );
            log.info("certificate blockchain event published, eventId={}, certificateId={}, blockHeight={}",
                    event.getEventId(), event.getCertificateId(), event.getBlockHeight());
        } catch (Exception e) {
            log.error("certificate blockchain event publish failed, eventId={}, certificateId={}",
                    event.getEventId(), event.getCertificateId(), e);
        }
    }
}

