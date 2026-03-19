package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.talent.CertificateVerifyBatchDto;
import com.xixi.pojo.dto.talent.CertificateVerifyByNumberDto;
import com.xixi.pojo.dto.talent.CertificateVerifyByQrcodeDto;
import com.xixi.pojo.dto.talent.TalentDashboardTrendQueryDto;
import com.xixi.pojo.vo.talent.TalentDashboardOverviewVo;
import com.xixi.pojo.vo.talent.TalentDashboardTrendPointVo;
import com.xixi.service.TalentCertificateDashboardService;
import com.xixi.util.HeaderParseUtil;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 证书验证编排与统计看板控制器。
 */
@RestController
@RequestMapping("/talent")
@RequiredArgsConstructor
public class TalentCertificateDashboardController {
    private final TalentCertificateDashboardService talentCertificateDashboardService;

    @MethodPurpose("证书编号验证：编排证书服务执行编号校验")
    @RoleRequired({RoleConstants.ENTERPRISE})
    @PostMapping("/certificate/verify/number")
    public Result verifyByNumber(
            @RequestBody CertificateVerifyByNumberDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        Object data = talentCertificateDashboardService.verifyByNumber(
                dto,
                HeaderParseUtil.parseUserId(userIdHeader),
                HeaderParseUtil.parseRole(roleHeader)
        );
        return Result.success(data);
    }

    @MethodPurpose("证书二维码验证：编排证书服务执行二维码校验")
    @RoleRequired({RoleConstants.ENTERPRISE})
    @PostMapping("/certificate/verify/qrcode")
    public Result verifyByQrcode(
            @RequestBody CertificateVerifyByQrcodeDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        Object data = talentCertificateDashboardService.verifyByQrcode(
                dto,
                HeaderParseUtil.parseUserId(userIdHeader),
                HeaderParseUtil.parseRole(roleHeader)
        );
        return Result.success(data);
    }

    @MethodPurpose("证书批量验证：编排证书服务批量校验")
    @RoleRequired({RoleConstants.ENTERPRISE})
    @PostMapping("/certificate/verify/batch")
    public Result verifyBatch(
            @RequestBody CertificateVerifyBatchDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        Object data = talentCertificateDashboardService.verifyBatch(
                dto,
                HeaderParseUtil.parseUserId(userIdHeader),
                HeaderParseUtil.parseRole(roleHeader)
        );
        return Result.success(data);
    }

    @MethodPurpose("证书验证历史：查询企业验证操作记录")
    @RoleRequired({RoleConstants.ENTERPRISE})
    @GetMapping("/certificate/verify/history")
    public Result getVerifyHistory(
            @RequestParam(value = "pageNum", required = false) Long pageNum,
            @RequestParam(value = "pageSize", required = false) Long pageSize,
            @RequestParam(value = "verificationResult", required = false) String verificationResult,
            @RequestParam(value = "verificationMethod", required = false) String verificationMethod,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        Object data = talentCertificateDashboardService.getVerifyHistory(
                pageNum,
                pageSize,
                verificationResult,
                verificationMethod,
                HeaderParseUtil.parseUserId(userIdHeader),
                HeaderParseUtil.parseRole(roleHeader)
        );
        return Result.success(data);
    }

    @MethodPurpose("证书验证报告：查询单次验证结果详情")
    @RoleRequired({RoleConstants.ENTERPRISE})
    @GetMapping("/certificate/verify/report/{verificationId}")
    public Result getVerifyReport(
            @PathVariable Long verificationId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        Object data = talentCertificateDashboardService.getVerifyReport(
                verificationId,
                HeaderParseUtil.parseUserId(userIdHeader),
                HeaderParseUtil.parseRole(roleHeader)
        );
        return Result.success(data);
    }

    @MethodPurpose("看板总览：查询企业或管理员目标企业运营汇总")
    @RoleRequired({RoleConstants.ENTERPRISE, RoleConstants.ADMIN})
    @GetMapping("/dashboard/overview")
    public Result getDashboardOverview(
            @RequestParam(value = "enterpriseId", required = false) Long enterpriseId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        TalentDashboardOverviewVo overview = talentCertificateDashboardService.getDashboardOverview(
                HeaderParseUtil.parseUserId(userIdHeader),
                HeaderParseUtil.parseRole(roleHeader),
                enterpriseId
        );
        return Result.success(overview);
    }

    @MethodPurpose("看板趋势：按指标查询企业区间趋势")
    @RoleRequired({RoleConstants.ENTERPRISE, RoleConstants.ADMIN})
    @GetMapping("/dashboard/trend")
    public Result getDashboardTrend(
            TalentDashboardTrendQueryDto query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        List<TalentDashboardTrendPointVo> trend = talentCertificateDashboardService.getDashboardTrend(
                query,
                HeaderParseUtil.parseUserId(userIdHeader),
                HeaderParseUtil.parseRole(roleHeader)
        );
        return Result.success(trend);
    }
}
