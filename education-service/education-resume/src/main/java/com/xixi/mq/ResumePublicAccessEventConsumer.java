package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 公开简历访问事件消费者。
 */
@Slf4j
@Component
public class ResumePublicAccessEventConsumer {

    /**
     * 消费公开简历访问事件。
     */
    @MethodPurpose("消费公开简历访问事件，用于后续访问日志与统计扩展")
    @RabbitListener(queues = ResumeMqConstant.RESUME_PUBLIC_ACCESS_QUEUE)
    public void consume(String messageBody) {
        try {
            ResumePublicAccessEvent event = JSONUtil.toBean(messageBody, ResumePublicAccessEvent.class);
            log.info("resume public access event consumed, eventId={}, type={}, resumeId={}, viewerId={}",
                    event.getEventId(), event.getEventType(), event.getResumeId(), event.getViewerId());
        } catch (Exception e) {
            log.error("resume public access event parse failed, body={}", messageBody, e);
        }
    }
}
