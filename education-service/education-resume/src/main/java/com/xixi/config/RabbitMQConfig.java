package com.xixi.config;

import com.xixi.annotation.MethodPurpose;
import com.xixi.mq.ResumeMqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 简历服务 RabbitMQ 资源声明。
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    @MethodPurpose("声明简历事件主题交换机")
    public TopicExchange resumeEventExchange() {
        return ExchangeBuilder.topicExchange(ResumeMqConstant.RESUME_EVENT_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    @MethodPurpose("声明简历变更队列")
    public Queue resumeChangedQueue() {
        return QueueBuilder.durable(ResumeMqConstant.RESUME_CHANGED_QUEUE).build();
    }

    @Bean
    @MethodPurpose("绑定简历变更队列到主题交换机")
    public Binding resumeChangedBinding() {
        return BindingBuilder.bind(resumeChangedQueue())
                .to(resumeEventExchange())
                .with(ResumeMqConstant.RESUME_CHANGED_ROUTING_KEY);
    }

    @Bean
    @MethodPurpose("声明简历证书关联变更队列")
    public Queue resumeCertificateChangedQueue() {
        return QueueBuilder.durable(ResumeMqConstant.RESUME_CERTIFICATE_CHANGED_QUEUE).build();
    }

    @Bean
    @MethodPurpose("绑定简历证书关联变更队列到主题交换机")
    public Binding resumeCertificateChangedBinding() {
        return BindingBuilder.bind(resumeCertificateChangedQueue())
                .to(resumeEventExchange())
                .with(ResumeMqConstant.RESUME_CERTIFICATE_CHANGED_ROUTING_KEY);
    }

    @Bean
    @MethodPurpose("声明简历浏览日志变更队列")
    public Queue resumeViewLogChangedQueue() {
        return QueueBuilder.durable(ResumeMqConstant.RESUME_VIEW_LOG_CHANGED_QUEUE).build();
    }

    @Bean
    @MethodPurpose("绑定简历浏览日志变更队列到主题交换机")
    public Binding resumeViewLogChangedBinding() {
        return BindingBuilder.bind(resumeViewLogChangedQueue())
                .to(resumeEventExchange())
                .with(ResumeMqConstant.RESUME_VIEW_LOG_CHANGED_ROUTING_KEY);
    }

    @Bean
    @MethodPurpose("声明公开简历访问队列")
    public Queue resumePublicAccessQueue() {
        return QueueBuilder.durable(ResumeMqConstant.RESUME_PUBLIC_ACCESS_QUEUE).build();
    }

    @Bean
    @MethodPurpose("绑定公开简历访问队列到主题交换机")
    public Binding resumePublicAccessBinding() {
        return BindingBuilder.bind(resumePublicAccessQueue())
                .to(resumeEventExchange())
                .with(ResumeMqConstant.RESUME_PUBLIC_ACCESS_ROUTING_KEY);
    }
}
