package com.xixi.service;

public interface SystemMessageDeliveryService {
    /**
     * 将已发布系统消息投递到用户消息表。
     *
     * @param systemMessageId 系统消息ID
     * @return 实际新增投递数量
     */
    int deliver(Long systemMessageId);
}
