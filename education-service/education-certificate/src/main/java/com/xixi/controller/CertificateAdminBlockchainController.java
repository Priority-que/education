package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.certificate.CertificateBatchAnchorDto;
import com.xixi.pojo.vo.certificate.CertificateAdminBlockchainPageVo;
import com.xixi.service.CertificateAdminBlockchainService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端证书上链接口。
 */
@RestController
@RequestMapping("/certificate/admin/blockchain")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ADMIN})
public class CertificateAdminBlockchainController {
    private final CertificateAdminBlockchainService certificateAdminBlockchainService;

    @MethodPurpose("管理端分页查询证书上链列表")
    @GetMapping("/page")
    public Result getCertificatePage(
            Long pageNum,
            Long pageSize,
            String certificateNumber,
            String status,
            Long blockHeight
    ) {
        IPage<CertificateAdminBlockchainPageVo> page = certificateAdminBlockchainService.getCertificatePage(
                pageNum,
                pageSize,
                certificateNumber,
                status,
                blockHeight
        );
        return Result.success(page);
    }

    @MethodPurpose("管理端触发证书上链（假链模拟）")
    @PostMapping("/anchor/{certificateId}")
    public Result anchorCertificate(
            @PathVariable Long certificateId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return certificateAdminBlockchainService.anchorCertificate(certificateId, parseUserId(userIdHeader));
    }

    @MethodPurpose("管理端批量触发证书上链（假链模拟）")
    @PostMapping("/anchor/batch")
    public Result anchorBatchCertificates(
            @RequestBody CertificateBatchAnchorDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return certificateAdminBlockchainService.anchorBatchCertificates(dto, parseUserId(userIdHeader));
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
