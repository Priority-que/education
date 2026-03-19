package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 简历浏览日志变更事件消费者。
 */
@Slf4j
@Component
public class ResumeViewLogChangedEventConsumer {

    /**
     * 消费简历浏览日志变更事件。
     */
    @MethodPurpose("消费简历浏览日志变更事件，用于后续扩展通知和审计链路")
    @RabbitListener(queues = ResumeMqConstant.RESUME_VIEW_LOG_CHANGED_QUEUE)
    public void consume(String messageBody) {
        try {
            ResumeViewLogChangedEvent event = JSONUtil.toBean(messageBody, ResumeViewLogChangedEvent.class);
            log.info("resume view log event consumed, eventId={}, type={}, viewLogId={}, resumeId={}",
                    event.getEventId(), event.getEventType(), event.getViewLogId(), event.getResumeId());
        } catch (Exception e) {
            log.error("resume view log event parse failed, body={}", messageBody, e);
        }
    }
}
