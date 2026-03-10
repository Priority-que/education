package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.resume.ResumeCertificateBindDto;
import com.xixi.service.ResumeCertificateService;
import com.xixi.support.StudentIdentityResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简历证书关联接口（8.1~8.3）。
 */
@RestController
@RequestMapping("/resume/certificate")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.STUDENT})
public class ResumeCertificateController {
    private final ResumeCertificateService resumeCertificateService;
    private final StudentIdentityResolver studentIdentityResolver;

    @MethodPurpose("8.1：绑定证书到简历")
    @PostMapping("/bind")
    public Result bindCertificate(
            @RequestBody ResumeCertificateBindDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return resumeCertificateService.bindCertificate(dto, resolveStudentId(userIdHeader));
    }

    @MethodPurpose("8.2：解绑简历证书关联")
    @DeleteMapping("/unbind/{id}")
    public Result unbindCertificate(
            @PathVariable Long id,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return resumeCertificateService.unbindCertificate(id, resolveStudentId(userIdHeader));
    }

    @MethodPurpose("8.3：查询简历证书关联列表")
    @GetMapping("/list/{resumeId}")
    public Result listCertificates(
            @PathVariable Long resumeId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return Result.success(resumeCertificateService.listCertificates(resumeId, resolveStudentId(userIdHeader)));
    }

    @MethodPurpose("将用户ID解析为学生ID")
    private Long resolveStudentId(String userIdHeader) {
        return studentIdentityResolver.resolveStudentIdByUserId(parseUserId(userIdHeader));
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
