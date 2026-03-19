package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.vo.certificate.CertificateBlockchainVo;
import com.xixi.service.CertificateBlockchainService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 区块链存证接口（9.1）。
 */
@RestController
@RequestMapping("/certificate/blockchain")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.STUDENT, RoleConstants.ENTERPRISE, RoleConstants.ADMIN})
public class CertificateBlockchainController {
    private final CertificateBlockchainService certificateBlockchainService;

    @MethodPurpose("9.1：查询证书区块链存证信息")
    @GetMapping("/{certificateId}")
    public Result getCertificateBlockchain(
            @PathVariable Long certificateId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String roleHeader
    ) {
        CertificateBlockchainVo vo = certificateBlockchainService.getCertificateBlockchain(
                certificateId,
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
}

