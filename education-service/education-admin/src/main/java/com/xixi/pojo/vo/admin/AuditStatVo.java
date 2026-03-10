package com.xixi.pojo.vo.admin;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 审核统计视图对象。
 */
@Data
public class AuditStatVo {
    private String auditType;
    private Integer totalCount;
    private Integer pendingCount;
    private Integer approvedCount;
    private Integer rejectedCount;
    private BigDecimal approveRate;
    private BigDecimal rejectRate;
}
