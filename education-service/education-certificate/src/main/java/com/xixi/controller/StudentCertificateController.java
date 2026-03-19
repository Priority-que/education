package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.query.certificate.CertificateMyQuery;
import com.xixi.pojo.vo.certificate.CertificateDetailVo;
import com.xixi.pojo.vo.certificate.CertificateMyPageVo;
import com.xixi.service.StudentCertificateService;
import com.xixi.support.StudentIdentityResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生证书接口（5.1~5.3）。
 */
@RestController
@RequestMapping("/certificate/my")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.STUDENT})
public class StudentCertificateController {
    private final StudentCertificateService studentCertificateService;
    private final StudentIdentityResolver studentIdentityResolver;

    @MethodPurpose("5.1：分页查询我的证书")
    @GetMapping("/page")
    public Result getMyCertificatePage(
            CertificateMyQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long studentId = studentIdentityResolver.resolveStudentIdByUserId(parseUserId(userIdHeader));
        IPage<CertificateMyPageVo> page = studentCertificateService.getMyCertificatePage(query, studentId);
        return Result.success(page);
    }

    @MethodPurpose("5.2：查询我的证书详情")
    @GetMapping("/detail/{certificateId}")
    public Result getMyCertificateDetail(
            @PathVariable Long certificateId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long studentId = studentIdentityResolver.resolveStudentIdByUserId(parseUserId(userIdHeader));
        CertificateDetailVo detailVo = studentCertificateService.getMyCertificateDetail(certificateId, studentId);
        return Result.success(detailVo);
    }

    @MethodPurpose("5.3：按证书编号查询我的证书详情")
    @GetMapping("/number/{certificateNumber}")
    public Result getMyCertificateByNumber(
            @PathVariable String certificateNumber,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long studentId = studentIdentityResolver.resolveStudentIdByUserId(parseUserId(userIdHeader));
        CertificateDetailVo detailVo = studentCertificateService.getMyCertificateByNumber(certificateNumber, studentId);
        return Result.success(detailVo);
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
}
