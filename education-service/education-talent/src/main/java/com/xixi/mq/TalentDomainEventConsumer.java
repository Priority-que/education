package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import com.xixi.service.TalentStatisticsAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 人才域事件消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TalentDomainEventConsumer {
    private final TalentStatisticsAsyncService talentStatisticsAsyncService;

    @MethodPurpose("消费人才域事件并异步更新统计快照")
    @RabbitListener(queues = TalentMqConstant.TALENT_DOMAIN_EVENT_QUEUE)
    public void consume(String messageBody) {
        try {
            TalentDomainEvent event = JSONUtil.toBean(messageBody, TalentDomainEvent.class);
            talentStatisticsAsyncService.consumeDomainEvent(event);
            log.info("talent domain event consumed, eventId={}, type={}, enterpriseId={}",
                    event.getEventId(), event.getEventType(), event.getEnterpriseId());
        } catch (Exception e) {
            log.error("talent domain event consume failed, body={}", messageBody, e);
        }
    }
}
