package com.xixi.service.impl;

import com.xixi.annotation.MethodPurpose;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.BlockchainRecord;
import com.xixi.entity.Certificate;
import com.xixi.exception.BizException;
import com.xixi.mapper.BlockchainRecordMapper;
import com.xixi.mapper.CertificateMapper;
import com.xixi.pojo.vo.certificate.CertificateBlockchainVo;
import com.xixi.service.CertificateBlockchainService;
import com.xixi.support.StudentIdentityResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 区块链存证查询服务实现（9.1）。
 */
@Service
@RequiredArgsConstructor
public class CertificateBlockchainServiceImpl implements CertificateBlockchainService {
    private final CertificateMapper certificateMapper;
    private final BlockchainRecordMapper blockchainRecordMapper;
    private final StudentIdentityResolver studentIdentityResolver;

    @Override
    @MethodPurpose("9.1：按证书ID查询区块链存证信息并校验访问权限")
    public CertificateBlockchainVo getCertificateBlockchain(Long certificateId, Long userId, Integer userRole) {
        if (certificateId == null) {
            throw new BizException(400, "certificateId不能为空");
        }
        if (userRole == null) {
            throw new BizException(401, "未登录或角色缺失");
        }
        Certificate certificate = certificateMapper.selectById(certificateId);
        if (certificate == null) {
            throw new BizException(404, "证书不存在");
        }

        if (RoleConstants.STUDENT == userRole) {
            Long studentId = studentIdentityResolver.resolveStudentIdByUserId(userId);
            if (!Objects.equals(certificate.getStudentId(), studentId)) {
                throw new BizException(403, "无权限查看他人证书存证");
            }
        } else if (RoleConstants.ENTERPRISE != userRole && RoleConstants.ADMIN != userRole) {
            throw new BizException(403, "当前角色不允许访问区块链存证");
        }

        BlockchainRecord record = queryBlockchainRecord(certificate);
        CertificateBlockchainVo vo = new CertificateBlockchainVo();
        vo.setCertificateId(certificate.getId());
        vo.setCertificateNumber(certificate.getCertificateNumber());
        vo.setCertificateStatus(certificate.getStatus());
        vo.setIssuingDate(certificate.getIssuingDate());
        vo.setCertificateHash(certificate.getHash());
        vo.setCertificatePreviousHash(certificate.getPreviousHash());
        vo.setBlockHeight(certificate.getBlockHeight());
        vo.setTransactionHash(certificate.getTransactionHash());
        if (record != null) {
            vo.setBlockchainRecordId(record.getId());
            vo.setRecordCurrentHash(record.getCurrentHash());
            vo.setRecordPreviousHash(record.getPreviousHash());
            vo.setMerkleRoot(record.getMerkleRoot());
            vo.setTimestamp(record.getTimestamp());
            vo.setNonce(record.getNonce());
            vo.setBlockSize(record.getBlockSize());
            vo.setTransactionCount(record.getTransactionCount());
            vo.setBlockchainCreatedTime(record.getCreatedTime());
        }
        return vo;
    }

    @MethodPurpose("按证书区块高度或证书哈希查询链上记录")
    private BlockchainRecord queryBlockchainRecord(Certificate certificate) {
        if (certificate.getBlockHeight() != null) {
            BlockchainRecord byHeight = blockchainRecordMapper.selectByBlockHeight(certificate.getBlockHeight());
            if (byHeight != null) {
                return byHeight;
            }
        }
        if (certificate.getHash() != null) {
            return blockchainRecordMapper.selectByCertificateHash(certificate.getHash());
        }
        return null;
    }
}
