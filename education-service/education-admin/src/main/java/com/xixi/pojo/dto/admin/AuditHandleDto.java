package com.xixi.pojo.dto.admin;

import lombok.Data;

/**
 * 通用审核处理请求参数。
 */
@Data
public class AuditHandleDto {
    /**
     * true-通过，false-拒绝。
     */
    private Boolean approved;
    private String auditOpinion;
    private String rejectReason;
}
