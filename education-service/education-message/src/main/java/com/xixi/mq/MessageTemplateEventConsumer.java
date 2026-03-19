package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 模板变更事件消费者（当前用于日志审计和链路观测）。
 */
@Slf4j
@Component
public class MessageTemplateEventConsumer {

    /**
     * 消费模板变更事件，记录审计日志。
     */
    @RabbitListener(queues = MessageMqConstant.MESSAGE_COMMAND_QUEUE)
    public void consume(String messageBody) {
        try {
            MessageTemplateChangedEvent event = JSONUtil.toBean(messageBody, MessageTemplateChangedEvent.class);
            if (event == null || event.getTemplateId() == null) {
                log.warn("收到无效模板变更事件, body={}", messageBody);
                return;
            }
            if (!"MESSAGE_TEMPLATE_CHANGED".equals(event.getEventType())) {
                log.info("收到其他命令消息, body={}", messageBody);
                return;
            }
            log.info("消费模板变更事件成功, eventId={}, action={}, templateId={}, operatorId={}",
                    event.getEventId(), event.getAction(), event.getTemplateId(), event.getOperatorId());
        } catch (Exception e) {
            log.error("消费模板变更事件失败, body={}", messageBody, e);
        }
    }
}
