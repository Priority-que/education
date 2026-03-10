package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.certificate.CertificateVerifyBatchDto;
import com.xixi.pojo.dto.certificate.CertificateVerifyByNumberDto;
import com.xixi.pojo.dto.certificate.CertificateVerifyByQrcodeDto;
import com.xixi.pojo.query.certificate.CertificateVerifyHistoryQuery;
import com.xixi.pojo.vo.certificate.CertificateVerifyBatchResultVo;
import com.xixi.pojo.vo.certificate.CertificateVerifyHistoryVo;
import com.xixi.pojo.vo.certificate.CertificateVerifyReportVo;
import com.xixi.pojo.vo.certificate.CertificateVerifyResultVo;
import com.xixi.service.CertificateVerificationService;
import com.xixi.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 证书验证接口（7.1~7.5）。
 */
@RestController
@RequestMapping("/certificate/verify")
@RequiredArgsConstructor
public class CertificateVerificationController {
    private final CertificateVerificationService certificateVerificationService;

    @MethodPurpose("7.1：输入证书编号执行验证")
    @PostMapping("/number")
    public Result verifyByNumber(
            @RequestBody CertificateVerifyByNumberDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader,
            HttpServletRequest request
    ) {
        CertificateVerifyResultVo vo = certificateVerificationService.verifyByNumber(
                dto,
                parseUserId(userIdHeader),
                parseUserRole(roleHeader),
                resolveClientIp(request),
                request.getHeader("User-Agent")
        );
        return Result.success(vo);
    }

    @MethodPurpose("7.2：输入二维码令牌执行验证")
    @PostMapping("/qrcode")
    public Result verifyByQrcode(
            @RequestBody CertificateVerifyByQrcodeDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader,
            HttpServletRequest request
    ) {
        CertificateVerifyResultVo vo = certificateVerificationService.verifyByQrcode(
                dto,
                parseUserId(userIdHeader),
                parseUserRole(roleHeader),
                resolveClientIp(request),
                request.getHeader("User-Agent")
        );
        return Result.success(vo);
    }

    @MethodPurpose("7.3：批量验证证书编号")
    @RoleRequired({RoleConstants.ENTERPRISE, RoleConstants.ADMIN})
    @PostMapping("/batch")
    public Result verifyBatch(
            @RequestBody CertificateVerifyBatchDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader,
            HttpServletRequest request
    ) {
        CertificateVerifyBatchResultVo vo = certificateVerificationService.verifyBatch(
                dto,
                parseUserId(userIdHeader),
                parseUserRole(roleHeader),
                resolveClientIp(request),
                request.getHeader("User-Agent")
        );
        return Result.success(vo);
    }

    @MethodPurpose("7.4：分页查询验证历史")
    @RoleRequired({RoleConstants.ENTERPRISE, RoleConstants.ADMIN})
    @GetMapping("/history")
    public Result getVerifyHistory(
            CertificateVerifyHistoryQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        IPage<CertificateVerifyHistoryVo> page = certificateVerificationService.getVerifyHistory(
                query,
                parseUserId(userIdHeader),
                parseUserRole(roleHeader)
        );
        return Result.success(page);
    }

    @MethodPurpose("7.5：查询验证报告详情")
    @RoleRequired({RoleConstants.ENTERPRISE, RoleConstants.ADMIN})
    @GetMapping("/report/{verificationId}")
    public Result getVerifyReport(
            @PathVariable Long verificationId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        CertificateVerifyReportVo vo = certificateVerificationService.getVerifyReport(
                verificationId,
                parseUserId(userIdHeader),
                parseUserRole(roleHeader)
        );
        return Result.success(vo);
    }

    @MethodPurpose("解析请求头中的用户ID")
    private Long parseUserId(String userIdHeader) {
        if (!StringUtils.hasText(userIdHeader)) {
            return null;
        }
        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @MethodPurpose("解析请求头中的角色编码")
    private Integer parseUserRole(String userRoleHeader) {
        if (!StringUtils.hasText(userRoleHeader)) {
            return null;
        }
        try {
            return Integer.parseInt(userRoleHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @MethodPurpose("解析客户端真实IP地址")
    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            String[] values = xForwardedFor.split(",");
            if (values.length > 0 && StringUtils.hasText(values[0])) {
                return values[0].trim();
            }
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}

