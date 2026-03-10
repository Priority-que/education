package com.xixi.openfeign.certificate;

import com.xixi.pojo.dto.admin.AdminBlockchainBatchAnchorDto;
import com.xixi.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 证书服务管理端接口远程调用。
 */
@FeignClient(name = "education-certificate", contextId = "educationCertificateAdminClient")
public interface EducationCertificateAdminClient {

    /**
     * 管理端分页查询证书上链列表。
     */
    @GetMapping("/certificate/admin/blockchain/page")
    Result getAdminBlockchainCertificatePage(
            @RequestParam(value = "pageNum", required = false) Long pageNum,
            @RequestParam(value = "pageSize", required = false) Long pageSize,
            @RequestParam(value = "certificateNumber", required = false) String certificateNumber,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "blockHeight", required = false) Long blockHeight,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    );

    /**
     * 触发单证书上链。
     */
    @PostMapping("/certificate/admin/blockchain/anchor/{certificateId}")
    Result anchorCertificate(
            @PathVariable("certificateId") Long certificateId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    );

    /**
     * 触发批量上链。
     */
    @PostMapping("/certificate/admin/blockchain/anchor/batch")
    Result anchorBatchCertificates(
            @RequestBody AdminBlockchainBatchAnchorDto dto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    );

    /**
     * 查询证书区块链详情。
     */
    @GetMapping("/certificate/blockchain/{certificateId}")
    Result getCertificateBlockchainDetail(
            @PathVariable("certificateId") Long certificateId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    );
}
