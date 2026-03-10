package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 证书主档变更事件消费者。
 */
@Slf4j
@Component
public class CertificateChangedEventConsumer {

    @MethodPurpose("消费证书主档变更事件，用于后续扩展消息通知与审计")
    @RabbitListener(queues = CertificateMqConstant.CERTIFICATE_CHANGED_QUEUE)
    public void consume(String messageBody) {
        try {
            CertificateChangedEvent event = JSONUtil.toBean(messageBody, CertificateChangedEvent.class);
            log.info("certificate event consumed, eventId={}, type={}, certificateId={}, status={}",
                    event.getEventId(), event.getEventType(), event.getCertificateId(), event.getStatus());
        } catch (Exception e) {
            log.error("certificate event parse failed, body={}", messageBody, e);
        }
    }
}

