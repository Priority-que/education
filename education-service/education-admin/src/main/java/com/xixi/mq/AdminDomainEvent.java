package com.xixi.mq;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理域通用事件。
 */
@Data
public class AdminDomainEvent {
    /**
     * 事件ID。
     */
    private String eventId;
    /**
     * 事件类型，例如 CREATE/UPDATE/DELETE/RELOAD。
     */
    private String eventType;
    /**
     * 业务类型，例如 AUDIT/CONFIG/MONITOR/OPERATION_LOG/DASHBOARD/BLOCKCHAIN。
     */
    private String bizType;
    /**
     * 业务主键。
     */
    private Long bizId;
    /**
     * 扩展载荷（JSON）。
     */
    private String payload;
    /**
     * 触发用户ID。
     */
    private Long operatorId;
    /**
     * 事件时间。
     */
    private LocalDateTime eventTime;
}
