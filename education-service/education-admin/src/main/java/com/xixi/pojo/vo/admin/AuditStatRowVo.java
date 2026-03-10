package com.xixi.pojo.vo.admin;

import lombok.Data;

/**
 * 审核统计行数据。
 */
@Data
public class AuditStatRowVo {
    private String auditType;
    private Integer totalCount;
    private Integer pendingCount;
    private Integer approvedCount;
    private Integer rejectedCount;
}
