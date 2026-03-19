package com.xixi.config;

import com.xixi.annotation.MethodPurpose;
import com.xixi.mq.TalentMqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 人才服务 RabbitMQ 资源声明。
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    @MethodPurpose("声明人才服务主题交换机")
    public TopicExchange talentEventExchange() {
        return ExchangeBuilder.topicExchange(TalentMqConstant.TALENT_EVENT_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    @MethodPurpose("声明人才域事件队列")
    public Queue talentDomainEventQueue() {
        return QueueBuilder.durable(TalentMqConstant.TALENT_DOMAIN_EVENT_QUEUE).build();
    }

    @Bean
    @MethodPurpose("绑定人才域事件队列到交换机")
    public Binding talentDomainEventBinding() {
        return BindingBuilder.bind(talentDomainEventQueue())
                .to(talentEventExchange())
                .with(TalentMqConstant.TALENT_DOMAIN_EVENT_ROUTING_KEY);
    }
}
