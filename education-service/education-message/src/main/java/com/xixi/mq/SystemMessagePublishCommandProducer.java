package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 系统消息发布命令生产者（事务提交后投递MQ）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMessagePublishCommandProducer {
    private static final String EVENT_TYPE = "SYSTEM_MESSAGE_PUBLISH";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布系统消息投递命令。
     */
    @MethodPurpose("构建系统消息发布命令并发布事务事件")
    public void publish(Long systemMessageId, Long operatorId, Integer expectedCount, LocalDateTime publishTime) {
        SystemMessagePublishCommand command = new SystemMessagePublishCommand();
        command.setEventId(UUID.randomUUID().toString().replace("-", ""));
        command.setEventType(EVENT_TYPE);
        command.setSystemMessageId(systemMessageId);
        command.setOperatorId(operatorId);
        command.setExpectedCount(expectedCount);
        command.setPublishTime(publishTime);
        applicationEventPublisher.publishEvent(command);
    }

    /**
     * 事务提交后发送系统消息投递命令。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @MethodPurpose("事务提交后发送系统消息发布命令到RabbitMQ")
    public void sendAfterCommit(SystemMessagePublishCommand command) {
        try {
            rabbitTemplate.convertAndSend(
                    MessageMqConstant.MESSAGE_COMMAND_EXCHANGE,
                    MessageMqConstant.SYSTEM_MESSAGE_PUBLISH_ROUTING_KEY,
                    JSONUtil.toJsonStr(command)
            );
            log.info("系统消息投递命令入队成功, eventId={}, systemMessageId={}, expectedCount={}",
                    command.getEventId(), command.getSystemMessageId(), command.getExpectedCount());
        } catch (Exception e) {
            log.error("系统消息投递命令入队失败, eventId={}, systemMessageId={}",
                    command.getEventId(), command.getSystemMessageId(), e);
        }
    }
}
