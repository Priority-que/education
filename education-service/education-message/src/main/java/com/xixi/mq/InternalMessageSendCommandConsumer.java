package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import com.xixi.exception.BizException;
import com.xixi.service.InternalMessageSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 内部消息投递命令消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InternalMessageSendCommandConsumer {
    private static final String IDEMPOTENT_KEY_PREFIX = "message:internal:send:consume:";

    private final StringRedisTemplate stringRedisTemplate;
    private final InternalMessageSendService internalMessageSendService;

    /**
     * 消费内部投递命令并落库用户消息。
     */
    @MethodPurpose("消费内部投递命令并执行用户消息落库")
    @RabbitListener(queues = MessageMqConstant.INTERNAL_SEND_QUEUE)
    public void consume(String messageBody) {
        InternalMessageSendCommand command;
        try {
            command = JSONUtil.toBean(messageBody, InternalMessageSendCommand.class);
        } catch (Exception e) {
            log.error("解析内部投递命令失败，body={}", messageBody, e);
            return;
        }

        if (command == null || command.getEventId() == null || command.getEventType() == null) {
            log.warn("内部投递命令参数不完整，body={}", messageBody);
            return;
        }

        if (!isSupportedEvent(command.getEventType())) {
            log.info("忽略不支持的内部投递事件，eventType={}, eventId={}",
                    command.getEventType(), command.getEventId());
            return;
        }

        String idempotentKey = IDEMPOTENT_KEY_PREFIX + command.getEventId();
        Boolean firstConsume = stringRedisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", Duration.ofDays(7));
        if (Boolean.FALSE.equals(firstConsume)) {
            log.info("内部投递命令重复消费，eventId={}, eventType={}",
                    command.getEventId(), command.getEventType());
            return;
        }

        try {
            int delivered = internalMessageSendService.consumeAsyncCommand(command);
            log.info("内部投递命令处理完成，eventId={}, eventType={}, delivered={}",
                    command.getEventId(), command.getEventType(), delivered);
        } catch (BizException e) {
            log.warn("内部投递命令业务校验失败，按已消费处理，eventId={}, eventType={}, message={}",
                    command.getEventId(), command.getEventType(), e.getMessage());
        } catch (Exception e) {
            stringRedisTemplate.delete(idempotentKey);
            log.error("内部投递命令处理失败，eventId={}, eventType={}",
                    command.getEventId(), command.getEventType(), e);
            throw e;
        }
    }

    private boolean isSupportedEvent(String eventType) {
        return InternalMessageSendCommand.EVENT_SEND_USER.equals(eventType)
                || InternalMessageSendCommand.EVENT_SEND_USERS.equals(eventType)
                || InternalMessageSendCommand.EVENT_SEND_ROLE.equals(eventType)
                || InternalMessageSendCommand.EVENT_SEND_TEMPLATE.equals(eventType);
    }
}
