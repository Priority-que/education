package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 我的消息变更事件消费者（用于审计与链路观测）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserMessageChangedEventConsumer {
    private static final String IDEMPOTENT_KEY_PREFIX = "message:user:change:consume:";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 消费我的消息变更事件并记录日志。
     */
    @MethodPurpose("消费我的消息变更事件并进行幂等审计日志记录")
    @RabbitListener(queues = MessageMqConstant.USER_MESSAGE_CHANGED_QUEUE)
    public void consume(String messageBody) {
        UserMessageChangedEvent event;
        try {
            event = JSONUtil.toBean(messageBody, UserMessageChangedEvent.class);
        } catch (Exception e) {
            log.error("解析我的消息变更事件失败，body={}", messageBody, e);
            return;
        }
        if (event == null || event.getEventId() == null || event.getAction() == null) {
            log.warn("我的消息变更事件参数不完整，body={}", messageBody);
            return;
        }
        if (!UserMessageChangedEvent.EVENT_TYPE.equals(event.getEventType())) {
            log.info("忽略非我的消息变更事件，eventType={}, body={}", event.getEventType(), messageBody);
            return;
        }

        String key = IDEMPOTENT_KEY_PREFIX + event.getEventId();
        Boolean firstConsume = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofDays(7));
        if (Boolean.FALSE.equals(firstConsume)) {
            log.info("我的消息变更事件重复消费，eventId={}, action={}", event.getEventId(), event.getAction());
            return;
        }

        log.info("消费我的消息变更事件成功，eventId={}, action={}, userId={}, affectedCount={}, messageType={}, messageIds={}",
                event.getEventId(), event.getAction(), event.getUserId(), event.getAffectedCount(),
                event.getMessageType(), event.getMessageIds());
    }
}

