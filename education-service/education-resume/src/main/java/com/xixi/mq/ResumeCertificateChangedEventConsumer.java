package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 简历证书关联变更事件消费者。
 */
@Slf4j
@Component
public class ResumeCertificateChangedEventConsumer {

    /**
     * 消费简历证书关联变更事件。
     */
    @MethodPurpose("消费简历证书关联变更事件，用于后续扩展证书联动或审计链路")
    @RabbitListener(queues = ResumeMqConstant.RESUME_CERTIFICATE_CHANGED_QUEUE)
    public void consume(String messageBody) {
        try {
            ResumeCertificateChangedEvent event = JSONUtil.toBean(messageBody, ResumeCertificateChangedEvent.class);
            log.info("resume certificate event consumed, eventId={}, type={}, resumeCertificateId={}, resumeId={}",
                    event.getEventId(), event.getEventType(), event.getResumeCertificateId(), event.getResumeId());
        } catch (Exception e) {
            log.error("resume certificate event parse failed, body={}", messageBody, e);
        }
    }
}
