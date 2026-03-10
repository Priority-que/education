package com.xixi.pojo.vo.certificate;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 证书规则视图对象。
 */
@Data
public class CertificateIssueRuleVo {
    private Long id;
    private Long courseId;
    private Boolean enabled;
    private String ruleName;
    private BigDecimal minFinalScore;
    private BigDecimal minCompletionRate;
    private Boolean requireExamPass;
    private String description;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
