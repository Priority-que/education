package com.xixi.config;

import com.xixi.mq.MessageMqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 消息服务 RabbitMQ 资源声明。
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange messageCommandExchange() {
        return ExchangeBuilder.topicExchange(MessageMqConstant.MESSAGE_COMMAND_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue messageCommandQueue() {
        return QueueBuilder.durable(MessageMqConstant.MESSAGE_COMMAND_QUEUE).build();
    }

    @Bean
    public Binding messageCommandBinding() {
        return BindingBuilder.bind(messageCommandQueue())
                .to(messageCommandExchange())
                .with(MessageMqConstant.TEMPLATE_CHANGE_ROUTING_KEY);
    }

    @Bean
    public Queue internalSendQueue() {
        return QueueBuilder.durable(MessageMqConstant.INTERNAL_SEND_QUEUE).build();
    }

    @Bean
    public Binding internalSendBinding() {
        return BindingBuilder.bind(internalSendQueue())
                .to(messageCommandExchange())
                .with(MessageMqConstant.INTERNAL_SEND_ROUTING_KEY_PATTERN);
    }

    @Bean
    public Queue userMessageChangedQueue() {
        return QueueBuilder.durable(MessageMqConstant.USER_MESSAGE_CHANGED_QUEUE).build();
    }

    @Bean
    public Binding userMessageChangedBinding() {
        return BindingBuilder.bind(userMessageChangedQueue())
                .to(messageCommandExchange())
                .with(MessageMqConstant.USER_MESSAGE_CHANGED_ROUTING_KEY);
    }

    @Bean
    public Queue systemMessagePublishQueue() {
        return QueueBuilder.durable(MessageMqConstant.SYSTEM_MESSAGE_PUBLISH_QUEUE).build();
    }

    @Bean
    public Binding systemMessagePublishBinding() {
        return BindingBuilder.bind(systemMessagePublishQueue())
                .to(messageCommandExchange())
                .with(MessageMqConstant.SYSTEM_MESSAGE_PUBLISH_ROUTING_KEY);
    }

    @Bean
    public TopicExchange messageRetryExchange() {
        return ExchangeBuilder.topicExchange(MessageMqConstant.MESSAGE_RETRY_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue messageRetryQueue() {
        return QueueBuilder.durable(MessageMqConstant.MESSAGE_RETRY_QUEUE).build();
    }

    @Bean
    public Binding messageRetryBinding() {
        return BindingBuilder.bind(messageRetryQueue())
                .to(messageRetryExchange())
                .with(MessageMqConstant.MESSAGE_RETRY_ROUTING_KEY_PATTERN);
    }

    @Bean
    public TopicExchange messageDlxExchange() {
        return ExchangeBuilder.topicExchange(MessageMqConstant.MESSAGE_DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue messageDlxQueue() {
        return QueueBuilder.durable(MessageMqConstant.MESSAGE_DLX_QUEUE).build();
    }

    @Bean
    public Binding messageDlxBinding() {
        return BindingBuilder.bind(messageDlxQueue())
                .to(messageDlxExchange())
                .with(MessageMqConstant.MESSAGE_DLX_ROUTING_KEY_PATTERN);
    }
}
