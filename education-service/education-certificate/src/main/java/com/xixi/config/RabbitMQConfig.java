package com.xixi.config;

import com.xixi.annotation.MethodPurpose;
import com.xixi.mq.CertificateMqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 证书服务 RabbitMQ 资源声明。
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    @MethodPurpose("声明证书服务主题交换机")
    public TopicExchange certificateEventExchange() {
        return ExchangeBuilder.topicExchange(CertificateMqConstant.CERTIFICATE_EVENT_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    @MethodPurpose("声明证书主档变更事件队列")
    public Queue certificateChangedQueue() {
        return QueueBuilder.durable(CertificateMqConstant.CERTIFICATE_CHANGED_QUEUE).build();
    }

    @Bean
    @MethodPurpose("绑定证书主档变更事件队列到交换机")
    public Binding certificateChangedBinding() {
        return BindingBuilder.bind(certificateChangedQueue())
                .to(certificateEventExchange())
                .with(CertificateMqConstant.CERTIFICATE_CHANGED_ROUTING_KEY);
    }

    @Bean
    @MethodPurpose("声明证书上链完成事件队列")
    public Queue certificateBlockchainAnchoredQueue() {
        return QueueBuilder.durable(CertificateMqConstant.CERTIFICATE_BLOCKCHAIN_ANCHORED_QUEUE).build();
    }

    @Bean
    @MethodPurpose("绑定证书上链完成事件队列到交换机")
    public Binding certificateBlockchainAnchoredBinding() {
        return BindingBuilder.bind(certificateBlockchainAnchoredQueue())
                .to(certificateEventExchange())
                .with(CertificateMqConstant.CERTIFICATE_BLOCKCHAIN_ANCHORED_ROUTING_KEY);
    }

    @Bean
    @MethodPurpose("声明证书分享变更事件队列")
    public Queue certificateShareChangedQueue() {
        return QueueBuilder.durable(CertificateMqConstant.CERTIFICATE_SHARE_CHANGED_QUEUE).build();
    }

    @Bean
    @MethodPurpose("绑定证书分享变更事件队列到交换机")
    public Binding certificateShareChangedBinding() {
        return BindingBuilder.bind(certificateShareChangedQueue())
                .to(certificateEventExchange())
                .with(CertificateMqConstant.CERTIFICATE_SHARE_CHANGED_ROUTING_KEY);
    }

    @Bean
    @MethodPurpose("声明证书验证变更事件队列")
    public Queue certificateVerificationChangedQueue() {
        return QueueBuilder.durable(CertificateMqConstant.CERTIFICATE_VERIFICATION_CHANGED_QUEUE).build();
    }

    @Bean
    @MethodPurpose("绑定证书验证变更事件队列到交换机")
    public Binding certificateVerificationChangedBinding() {
        return BindingBuilder.bind(certificateVerificationChangedQueue())
                .to(certificateEventExchange())
                .with(CertificateMqConstant.CERTIFICATE_VERIFICATION_CHANGED_ROUTING_KEY);
    }
}
