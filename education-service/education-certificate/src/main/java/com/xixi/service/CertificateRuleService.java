package com.xixi.service;

import com.xixi.pojo.dto.certificate.CertificateIssueRuleSaveDto;
import com.xixi.pojo.vo.certificate.CertificateIssueRuleVo;
import com.xixi.web.Result;

/**
 * 证书规则服务接口。
 */
public interface CertificateRuleService {
    CertificateIssueRuleVo getByCourseId(Long courseId, Long teacherId);

    Result createRule(CertificateIssueRuleSaveDto dto, Long teacherId);

    Result updateRule(Long ruleId, CertificateIssueRuleSaveDto dto, Long teacherId);

    Result deleteRule(Long ruleId, Long teacherId);
}
