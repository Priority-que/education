package com.xixi.pojo.dto.certificate;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 证书规则创建/更新参数。
 */
@Data
public class CertificateIssueRuleSaveDto {
    private Long courseId;
    private Boolean enabled;
    private String ruleName;
    private BigDecimal minFinalScore;
    private BigDecimal minCompletionRate;
    private Boolean requireExamPass;
    private String description;
}
