package com.xixi.config;

import com.xixi.annotation.MethodPurpose;
import com.xixi.mq.AdminMqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 管理服务 RabbitMQ 资源声明。
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    @MethodPurpose("声明管理域主题交换机")
    public TopicExchange adminEventExchange() {
        return ExchangeBuilder.topicExchange(AdminMqConstant.ADMIN_EVENT_EXCHANGE).durable(true).build();
    }

    @Bean
    @MethodPurpose("声明管理域事件队列")
    public Queue adminEventQueue() {
        return QueueBuilder.durable(AdminMqConstant.ADMIN_EVENT_QUEUE).build();
    }

    @Bean
    @MethodPurpose("绑定管理域事件队列到交换机")
    public Binding adminEventBinding() {
        return BindingBuilder.bind(adminEventQueue())
                .to(adminEventExchange())
                .with(AdminMqConstant.ADMIN_EVENT_ROUTING_KEY);
    }
}
