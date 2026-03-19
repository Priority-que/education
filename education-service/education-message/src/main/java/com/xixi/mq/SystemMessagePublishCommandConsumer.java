package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import com.xixi.service.SystemMessageDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 系统消息发布命令消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMessagePublishCommandConsumer {
    private static final String EVENT_TYPE = "SYSTEM_MESSAGE_PUBLISH";
    private static final String IDEMPOTENT_KEY_PREFIX = "message:system:publish:consume:";

    private final SystemMessageDeliveryService systemMessageDeliveryService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 消费系统消息发布命令并异步落用户消息。
     */
    @RabbitListener(queues = MessageMqConstant.SYSTEM_MESSAGE_PUBLISH_QUEUE)
    @MethodPurpose("消费系统消息发布命令并执行用户消息投递")
    public void consume(String messageBody) {
        SystemMessagePublishCommand command;
        try {
            command = JSONUtil.toBean(messageBody, SystemMessagePublishCommand.class);
        } catch (Exception e) {
            log.error("解析系统消息发布命令失败, body={}", messageBody, e);
            return;
        }
        if (command == null || command.getSystemMessageId() == null || command.getEventId() == null) {
            log.warn("系统消息发布命令参数不完整, body={}", messageBody);
            return;
        }
        if (!EVENT_TYPE.equals(command.getEventType())) {
            log.info("忽略非系统消息发布命令, eventType={}, body={}", command.getEventType(), messageBody);
            return;
        }

        String idempotentKey = IDEMPOTENT_KEY_PREFIX + command.getEventId();
        Boolean firstConsume = stringRedisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", Duration.ofDays(7));
        if (Boolean.FALSE.equals(firstConsume)) {
            log.info("系统消息发布命令重复消费, eventId={}, systemMessageId={}",
                    command.getEventId(), command.getSystemMessageId());
            return;
        }

        try {
            int delivered = systemMessageDeliveryService.deliver(command.getSystemMessageId());
            log.info("系统消息投递完成, eventId={}, systemMessageId={}, delivered={}",
                    command.getEventId(), command.getSystemMessageId(), delivered);
        } catch (Exception e) {
            stringRedisTemplate.delete(idempotentKey);
            log.error("系统消息投递失败, eventId={}, systemMessageId={}",
                    command.getEventId(), command.getSystemMessageId(), e);
            throw e;
        }
    }
}
