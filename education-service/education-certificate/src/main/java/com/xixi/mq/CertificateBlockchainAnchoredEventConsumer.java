package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 证书上链事件消费者。
 */
@Slf4j
@Component
public class CertificateBlockchainAnchoredEventConsumer {

    @MethodPurpose("消费证书上链事件，用于后续扩展通知和审计")
    @RabbitListener(queues = CertificateMqConstant.CERTIFICATE_BLOCKCHAIN_ANCHORED_QUEUE)
    public void consume(String messageBody) {
        try {
            CertificateBlockchainAnchoredEvent event = JSONUtil.toBean(messageBody, CertificateBlockchainAnchoredEvent.class);
            log.info("certificate blockchain event consumed, eventId={}, certificateId={}, blockHeight={}",
                    event.getEventId(), event.getCertificateId(), event.getBlockHeight());
        } catch (Exception e) {
            log.error("certificate blockchain event parse failed, body={}", messageBody, e);
        }
    }
}

