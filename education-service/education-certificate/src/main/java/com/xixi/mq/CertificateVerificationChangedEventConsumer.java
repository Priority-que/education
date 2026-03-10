package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 证书验证变更事件消费者。
 */
@Slf4j
@Component
public class CertificateVerificationChangedEventConsumer {

    @MethodPurpose("消费证书验证变更事件，用于后续扩展通知与审计链路")
    @RabbitListener(queues = CertificateMqConstant.CERTIFICATE_VERIFICATION_CHANGED_QUEUE)
    public void consume(String messageBody) {
        try {
            CertificateVerificationChangedEvent event = JSONUtil.toBean(messageBody, CertificateVerificationChangedEvent.class);
            log.info("certificate verification event consumed, eventId={}, verificationId={}, result={}",
                    event.getEventId(), event.getVerificationId(), event.getVerificationResult());
        } catch (Exception e) {
            log.error("certificate verification event parse failed, body={}", messageBody, e);
        }
    }
}

