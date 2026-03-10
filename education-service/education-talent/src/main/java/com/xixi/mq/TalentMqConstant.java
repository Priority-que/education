package com.xixi.mq;

/**
 * 人才服务 MQ 常量。
 */
public final class TalentMqConstant {
    public static final String TALENT_EVENT_EXCHANGE = "education.talent.event.exchange";
    public static final String TALENT_DOMAIN_EVENT_QUEUE = "education.talent.domain.event.queue";
    public static final String TALENT_DOMAIN_EVENT_ROUTING_KEY = "talent.domain.event";

    private TalentMqConstant() {
    }
}
