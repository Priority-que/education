package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 管理域事件消费者。
 */
@Slf4j
@Component
public class AdminDomainEventConsumer {

    @MethodPurpose("消费管理域事件，用于后续扩展通知与审计")
    @RabbitListener(queues = AdminMqConstant.ADMIN_EVENT_QUEUE)
    public void consume(String messageBody) {
        try {
            AdminDomainEvent event = JSONUtil.toBean(messageBody, AdminDomainEvent.class);
            log.info("admin event consumed, eventId={}, bizType={}, bizId={}, eventType={}",
                    event.getEventId(), event.getBizType(), event.getBizId(), event.getEventType());
        } catch (Exception e) {
            log.error("admin event parse failed, body={}", messageBody, e);
        }
    }
}
