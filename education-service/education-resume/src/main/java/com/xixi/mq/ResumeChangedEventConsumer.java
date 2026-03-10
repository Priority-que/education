package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 简历变更事件消费者。
 */
@Slf4j
@Component
public class ResumeChangedEventConsumer {

    /**
     * 消费简历主档变更事件。
     */
    @MethodPurpose("消费简历主档变更事件，用于后续扩展搜索索引或通知链路")
    @RabbitListener(queues = ResumeMqConstant.RESUME_CHANGED_QUEUE)
    public void consume(String messageBody) {
        try {
            ResumeChangedEvent event = JSONUtil.toBean(messageBody, ResumeChangedEvent.class);
            log.info("resume event consumed, eventId={}, type={}, resumeId={}, studentId={}",
                    event.getEventId(), event.getEventType(), event.getResumeId(), event.getStudentId());
        } catch (Exception e) {
            log.error("resume event parse failed, body={}", messageBody, e);
        }
    }
}
