package com.xixi.mq;

/**
 * 简历服务 RabbitMQ 资源常量。
 */
public final class ResumeMqConstant {
    public static final String RESUME_EVENT_EXCHANGE = "education.resume.event.exchange";
    public static final String RESUME_CHANGED_QUEUE = "education.resume.changed.queue";
    public static final String RESUME_CHANGED_ROUTING_KEY = "resume.changed";
    public static final String RESUME_CERTIFICATE_CHANGED_QUEUE = "education.resume.certificate.changed.queue";
    public static final String RESUME_CERTIFICATE_CHANGED_ROUTING_KEY = "resume.certificate.changed";
    public static final String RESUME_VIEW_LOG_CHANGED_QUEUE = "education.resume.view.log.changed.queue";
    public static final String RESUME_VIEW_LOG_CHANGED_ROUTING_KEY = "resume.view.log.changed";
    public static final String RESUME_PUBLIC_ACCESS_QUEUE = "education.resume.public.access.queue";
    public static final String RESUME_PUBLIC_ACCESS_ROUTING_KEY = "resume.public.access";

    private ResumeMqConstant() {
    }
}
