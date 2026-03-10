package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.EnterpriseVerificationApplyDto;
import com.xixi.pojo.dto.EnterpriseVerificationAuditDto;
import com.xixi.pojo.query.EnterpriseVerificationHistoryQuery;
import com.xixi.pojo.vo.EnterpriseVerificationVo;
import com.xixi.web.Result;

public interface EnterpriseVerificationService {
    Result apply(EnterpriseVerificationApplyDto dto, Long userId);

    EnterpriseVerificationVo current(Long userId);

    IPage<EnterpriseVerificationVo> historyMyPage(EnterpriseVerificationHistoryQuery query, Long userId);

    IPage<EnterpriseVerificationVo> historyAdminPage(EnterpriseVerificationHistoryQuery query, Long userId, Integer userRole);

    IPage<EnterpriseVerificationVo> historyPage(EnterpriseVerificationHistoryQuery query, Long userId, Integer userRole);

    Result audit(Long applicationId, EnterpriseVerificationAuditDto dto, Long auditorId, Integer auditorRole);
}
