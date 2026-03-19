package com.xixi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.pojo.dto.admin.AdminBlockchainBatchAnchorDto;
import com.xixi.pojo.dto.admin.BlockchainNodeStatusUpdateDto;
import com.xixi.pojo.query.admin.BlockchainCertificatePageQuery;
import com.xixi.pojo.query.admin.BlockchainNodePageQuery;
import com.xixi.pojo.vo.admin.BlockchainCertificatePageVo;
import com.xixi.pojo.vo.admin.BlockchainNodeVo;
import com.xixi.pojo.vo.admin.BlockchainStatusVo;
import com.xixi.web.Result;

/**
 * 区块链管理编排服务接口。
 */
public interface AdminBlockchainService {
    IPage<BlockchainCertificatePageVo> getBlockchainCertificatePage(BlockchainCertificatePageQuery query, Long adminId);

    Object getBlockchainCertificateDetail(Long certificateId, Long adminId);

    Result anchorCertificate(Long certificateId, Long adminId);

    Result anchorBatchCertificates(AdminBlockchainBatchAnchorDto dto, Long adminId);

    BlockchainStatusVo getBlockchainStatus();

    IPage<BlockchainNodeVo> getBlockchainNodePage(BlockchainNodePageQuery query, Long adminId);

    BlockchainNodeVo getBlockchainNodeDetail(Long nodeId, Long adminId);

    Result updateBlockchainNodeStatus(Long nodeId, BlockchainNodeStatusUpdateDto dto, Long adminId);

    Result syncBlockchainNode(Long nodeId, Long adminId);
}
