package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.certificate.CertificateShareCreateDto;
import com.xixi.pojo.query.certificate.CertificateShareMyQuery;
import com.xixi.pojo.vo.certificate.CertificatePublicShareVo;
import com.xixi.pojo.vo.certificate.CertificateShareVo;
import com.xixi.service.CertificateShareService;
import com.xixi.support.StudentIdentityResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 证书分享接口（6.1~6.4）。
 */
@RestController
@RequestMapping("/certificate/share")
@RequiredArgsConstructor
public class CertificateShareController {
    private final CertificateShareService certificateShareService;
    private final StudentIdentityResolver studentIdentityResolver;

    @MethodPurpose("6.1：创建证书分享链接")
    @RoleRequired({RoleConstants.STUDENT})
    @PostMapping("/create")
    public Result createShare(
            @RequestBody CertificateShareCreateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long studentId = studentIdentityResolver.resolveStudentIdByUserId(parseUserId(userIdHeader));
        return certificateShareService.createShare(dto, studentId);
    }

    @MethodPurpose("6.2：失效证书分享链接")
    @RoleRequired({RoleConstants.STUDENT})
    @PutMapping("/revoke/{shareId}")
    public Result revokeShare(
            @PathVariable Long shareId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long studentId = studentIdentityResolver.resolveStudentIdByUserId(parseUserId(userIdHeader));
        return certificateShareService.revokeShare(shareId, studentId);
    }

    @MethodPurpose("6.3：分页查询我的证书分享记录")
    @RoleRequired({RoleConstants.STUDENT})
    @GetMapping("/my/page")
    public Result getMySharePage(
            CertificateShareMyQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        Long studentId = studentIdentityResolver.resolveStudentIdByUserId(parseUserId(userIdHeader));
        IPage<CertificateShareVo> page = certificateShareService.getMySharePage(query, studentId);
        return Result.success(page);
    }

    @MethodPurpose("6.4：公开访问分享证书")
    @GetMapping("/public/{shareToken}")
    public Result getPublicShareDetail(@PathVariable String shareToken) {
        CertificatePublicShareVo detailVo = certificateShareService.getPublicShareDetail(shareToken);
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
