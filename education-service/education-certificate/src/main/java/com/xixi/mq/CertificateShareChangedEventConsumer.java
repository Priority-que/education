package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 证书分享变更事件消费者。
 */
@Slf4j
@Component
public class CertificateShareChangedEventConsumer {

    @MethodPurpose("消费证书分享变更事件，用于后续扩展通知与审计链路")
    @RabbitListener(queues = CertificateMqConstant.CERTIFICATE_SHARE_CHANGED_QUEUE)
    public void consume(String messageBody) {
        try {
            CertificateShareChangedEvent event = JSONUtil.toBean(messageBody, CertificateShareChangedEvent.class);
            log.info("certificate share event consumed, eventId={}, type={}, shareId={}, certificateId={}",
                    event.getEventId(), event.getEventType(), event.getShareId(), event.getCertificateId());
        } catch (Exception e) {
            log.error("certificate share event parse failed, body={}", messageBody, e);
        }
    }
}

