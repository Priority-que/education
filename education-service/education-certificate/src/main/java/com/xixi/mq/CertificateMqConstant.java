package com.xixi.mq;

/**
 * 证书服务 MQ 常量。
 */
public final class CertificateMqConstant {
    public static final String CERTIFICATE_EVENT_EXCHANGE = "education.certificate.event.exchange";
    public static final String CERTIFICATE_CHANGED_QUEUE = "education.certificate.changed.queue";
    public static final String CERTIFICATE_CHANGED_ROUTING_KEY = "certificate.changed";
    public static final String CERTIFICATE_BLOCKCHAIN_ANCHORED_QUEUE = "education.certificate.blockchain.anchored.queue";
    public static final String CERTIFICATE_BLOCKCHAIN_ANCHORED_ROUTING_KEY = "certificate.blockchain.anchored";
    public static final String CERTIFICATE_SHARE_CHANGED_QUEUE = "education.certificate.share.changed.queue";
    public static final String CERTIFICATE_SHARE_CHANGED_ROUTING_KEY = "certificate.share.changed";
    public static final String CERTIFICATE_VERIFICATION_CHANGED_QUEUE = "education.certificate.verification.changed.queue";
    public static final String CERTIFICATE_VERIFICATION_CHANGED_ROUTING_KEY = "certificate.verification.changed";

    private CertificateMqConstant() {
    }
}
