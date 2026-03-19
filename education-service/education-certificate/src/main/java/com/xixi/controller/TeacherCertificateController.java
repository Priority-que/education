package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.certificate.CertificateIssueDto;
import com.xixi.pojo.dto.certificate.CertificateRevokeDto;
import com.xixi.pojo.query.certificate.CertificateTeacherIssuedQuery;
import com.xixi.pojo.vo.certificate.CertificateDetailVo;
import com.xixi.pojo.vo.certificate.CertificateTeacherIssuedVo;
import com.xixi.service.TeacherCertificateService;
import com.xixi.support.TeacherIdentityResolver;
import com.xixi.web.Result;
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
 * 教师证书管理接口（8.1~8.3）。
 */
@RestController
@RequestMapping("/certificate/teacher")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.TEACHER})
public class TeacherCertificateController {
    private final TeacherCertificateService teacherCertificateService;
    private final TeacherIdentityResolver teacherIdentityResolver;

    @MethodPurpose("8.1：教师手动颁发证书")
    @PostMapping("/issue")
    public Result issueCertificate(
            @RequestBody CertificateIssueDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long teacherId = teacherIdentityResolver.resolveTeacherIdByUserId(parseUserId(userIdHeader));
        return teacherCertificateService.issueCertificate(dto, teacherId);
    }

    @MethodPurpose("8.2：教师撤销证书")
    @PostMapping("/revoke/{certificateId}")
    public Result revokeCertificate(
            @PathVariable Long certificateId,
            @RequestBody(required = false) CertificateRevokeDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long teacherId = teacherIdentityResolver.resolveTeacherIdByUserId(parseUserId(userIdHeader));
        return teacherCertificateService.revokeCertificate(certificateId, dto, teacherId);
    }

    @MethodPurpose("8.3：分页查询教师已颁发证书")
    @GetMapping("/issued/page")
    public Result getTeacherIssuedPage(
            CertificateTeacherIssuedQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long teacherId = teacherIdentityResolver.resolveTeacherIdByUserId(parseUserId(userIdHeader));
        IPage<CertificateTeacherIssuedVo> page = teacherCertificateService.getTeacherIssuedPage(query, teacherId);
        return Result.success(page);
    }

    @MethodPurpose("8.3-扩展：教师查看自己已颁发证书详情")
    @GetMapping("/issued/detail/{certificateId}")
    public Result getTeacherIssuedDetail(
            @PathVariable Long certificateId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long teacherId = teacherIdentityResolver.resolveTeacherIdByUserId(parseUserId(userIdHeader));
        CertificateDetailVo detailVo = teacherCertificateService.getTeacherIssuedDetail(certificateId, teacherId);
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
