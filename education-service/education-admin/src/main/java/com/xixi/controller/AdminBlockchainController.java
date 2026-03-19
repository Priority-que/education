package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.pojo.dto.admin.AdminBlockchainBatchAnchorDto;
import com.xixi.pojo.dto.admin.BlockchainNodeStatusUpdateDto;
import com.xixi.pojo.query.admin.BlockchainCertificatePageQuery;
import com.xixi.pojo.query.admin.BlockchainNodePageQuery;
import com.xixi.pojo.vo.admin.BlockchainCertificatePageVo;
import com.xixi.pojo.vo.admin.BlockchainNodeVo;
import com.xixi.service.AdminBlockchainService;
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
 * 区块链管理接口。
 */
@RestController
@RequestMapping("/admin/blockchain")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.ADMIN})
public class AdminBlockchainController {
    private final AdminBlockchainService adminBlockchainService;

    @MethodPurpose("分页查询区块链证书记录")
    @GetMapping("/certificate/page")
    public Result getCertificatePage(
            BlockchainCertificatePageQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<BlockchainCertificatePageVo> page = adminBlockchainService.getBlockchainCertificatePage(query, parseUserId(userIdHeader));
        return Result.success(page);
    }

    @MethodPurpose("查询单证书存证明细")
    @GetMapping("/certificate/{certificateId}")
    public Result getCertificateDetail(
            @PathVariable Long certificateId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return Result.success(adminBlockchainService.getBlockchainCertificateDetail(certificateId, parseUserId(userIdHeader)));
    }

    @MethodPurpose("触发单证书上链")
    @PostMapping("/certificate/anchor/{certificateId}")
    public Result anchorCertificate(
            @PathVariable Long certificateId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminBlockchainService.anchorCertificate(certificateId, parseUserId(userIdHeader));
    }

    @MethodPurpose("触发批量证书上链")
    @PostMapping("/certificate/anchor/batch")
    public Result anchorBatch(
            @RequestBody AdminBlockchainBatchAnchorDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminBlockchainService.anchorBatchCertificates(dto, parseUserId(userIdHeader));
    }

    @MethodPurpose("查询区块链状态")
    @GetMapping("/status")
    public Result getBlockchainStatus() {
        return Result.success(adminBlockchainService.getBlockchainStatus());
    }

    @MethodPurpose("分页查询区块链节点")
    @GetMapping("/node/page")
    public Result getBlockchainNodePage(
            BlockchainNodePageQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<BlockchainNodeVo> page = adminBlockchainService.getBlockchainNodePage(query, parseUserId(userIdHeader));
        return Result.success(page);
    }

    @MethodPurpose("查询区块链节点详情")
    @GetMapping("/node/detail/{nodeId}")
    public Result getBlockchainNodeDetail(
            @PathVariable Long nodeId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return Result.success(adminBlockchainService.getBlockchainNodeDetail(nodeId, parseUserId(userIdHeader)));
    }

    @MethodPurpose("更新区块链节点状态")
    @PutMapping("/node/status/{nodeId}")
    public Result updateBlockchainNodeStatus(
            @PathVariable Long nodeId,
            @RequestBody BlockchainNodeStatusUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminBlockchainService.updateBlockchainNodeStatus(nodeId, dto, parseUserId(userIdHeader));
    }

    @MethodPurpose("触发区块链节点同步")
    @PostMapping("/node/sync/{nodeId}")
    public Result syncBlockchainNode(
            @PathVariable Long nodeId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return adminBlockchainService.syncBlockchainNode(nodeId, parseUserId(userIdHeader));
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
