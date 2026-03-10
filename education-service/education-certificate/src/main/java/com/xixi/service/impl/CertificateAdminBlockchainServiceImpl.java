package com.xixi.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.BlockchainRecord;
import com.xixi.entity.Certificate;
import com.xixi.exception.BizException;
import com.xixi.mapper.BlockchainRecordMapper;
import com.xixi.mapper.CertificateMapper;
import com.xixi.mq.CertificateBlockchainAnchoredEventProducer;
import com.xixi.pojo.dto.certificate.CertificateBatchAnchorDto;
import com.xixi.pojo.vo.certificate.CertificateAdminBlockchainPageVo;
import com.xixi.pojo.vo.certificate.CertificateAnchorResultVo;
import com.xixi.pojo.vo.certificate.CertificateBatchAnchorFailItemVo;
import com.xixi.pojo.vo.certificate.CertificateBatchAnchorResultVo;
import com.xixi.service.CertificateAdminBlockchainService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 管理端证书上链服务实现（假链模拟）。
 */
@Service
@RequiredArgsConstructor
public class CertificateAdminBlockchainServiceImpl implements CertificateAdminBlockchainService {
    private static final String STATUS_ISSUED = "ISSUED";
    private static final String GENESIS_PREVIOUS_HASH = "0x0000000000000000000000000000000000000000000000000000000000000000";

    private final CertificateMapper certificateMapper;
    private final BlockchainRecordMapper blockchainRecordMapper;
    private final CertificateBlockchainAnchoredEventProducer certificateBlockchainAnchoredEventProducer;
    private final ObjectProvider<CertificateAdminBlockchainService> certificateAdminBlockchainServiceProvider;

    @Override
    @MethodPurpose("管理端分页查询证书上链列表（基于证书主表）")
    public IPage<CertificateAdminBlockchainPageVo> getCertificatePage(
            Long pageNum,
            Long pageSize,
            String certificateNumber,
            String status,
            Long blockHeight
    ) {
        long current = pageNum == null || pageNum <= 0 ? 1L : pageNum;
        long size = pageSize == null || pageSize <= 0 ? 10L : pageSize;
        String numberKeyword = StringUtils.hasText(certificateNumber) ? certificateNumber.trim() : null;
        String normalizedStatus = StringUtils.hasText(status) ? status.trim().toUpperCase() : null;

        LambdaQueryWrapper<Certificate> wrapper = Wrappers.lambdaQuery(Certificate.class)
                .orderByDesc(Certificate::getIssuingDate)
                .orderByDesc(Certificate::getCreatedTime);
        if (StringUtils.hasText(numberKeyword)) {
            wrapper.like(Certificate::getCertificateNumber, numberKeyword);
        }
        if (blockHeight != null) {
            wrapper.eq(Certificate::getBlockHeight, blockHeight);
        }
        applyStatusFilter(wrapper, normalizedStatus);

        Page<Certificate> entityPage = certificateMapper.selectPage(new Page<>(current, size), wrapper);
        Page<CertificateAdminBlockchainPageVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toPageVo).toList());
        voPage.setPages(entityPage.getPages());
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("管理端触发证书上链，模拟写入区块链记录并回填证书区块字段")
    public Result anchorCertificate(Long certificateId, Long operatorId) {
        if (operatorId == null) {
            throw new BizException(401, "未登录或用户ID缺失");
        }
        if (certificateId == null) {
            throw new BizException(400, "certificateId不能为空");
        }

        Certificate certificate = certificateMapper.selectById(certificateId);
        if (certificate == null) {
            throw new BizException(404, "证书不存在");
        }
        if (!STATUS_ISSUED.equalsIgnoreCase(certificate.getStatus())) {
            throw new BizException(409, "仅ISSUED状态证书允许上链");
        }

        CertificateAnchorResultVo anchored = queryAnchoredResult(certificateId);
        if (anchored != null && Boolean.TRUE.equals(anchored.getAlreadyAnchored())) {
            return Result.success("证书已上链", anchored);
        }

        BlockchainRecord createdRecord = tryCreateBlockRecord(certificate);
        String transactionHash = buildTransactionHash(certificate.getId(), createdRecord.getId(), createdRecord.getTimestamp());
        certificate.setBlockHeight(createdRecord.getBlockHeight());
        certificate.setTransactionHash(transactionHash);
        certificate.setPreviousHash(createdRecord.getPreviousHash());
        certificate.setUpdatedTime(LocalDateTime.now());
        certificateMapper.updateById(certificate);

        certificateBlockchainAnchoredEventProducer.publish(
                certificate.getId(),
                certificate.getCertificateNumber(),
                createdRecord.getBlockHeight(),
                transactionHash,
                createdRecord.getCurrentHash(),
                operatorId
        );

        CertificateAnchorResultVo resultVo = toAnchorResult(certificate, createdRecord, false);
        return Result.success("证书上链成功", resultVo);
    }

    @Override
    @MethodPurpose("管理端批量触发证书上链，按单证书事务逐个处理并返回明细")
    public Result anchorBatchCertificates(CertificateBatchAnchorDto dto, Long operatorId) {
        if (operatorId == null) {
            throw new BizException(401, "未登录或用户ID缺失");
        }
        List<Long> certificateIds = normalizeCertificateIds(dto);
        if (certificateIds.isEmpty()) {
            throw new BizException(400, "certificateIds不能为空");
        }
        if (certificateIds.size() > 200) {
            throw new BizException(400, "单次批量上链最多支持200条");
        }

        List<CertificateAnchorResultVo> successItems = new ArrayList<>();
        List<CertificateBatchAnchorFailItemVo> failedItems = new ArrayList<>();
        CertificateAdminBlockchainService proxyService = certificateAdminBlockchainServiceProvider.getObject();

        for (Long certificateId : certificateIds) {
            try {
                Result singleResult = proxyService.anchorCertificate(certificateId, operatorId);
                CertificateAnchorResultVo singleVo = unwrapAnchorResult(singleResult, certificateId);
                if (singleVo != null) {
                    successItems.add(singleVo);
                    continue;
                }
                CertificateBatchAnchorFailItemVo failItem = new CertificateBatchAnchorFailItemVo();
                failItem.setCertificateId(certificateId);
                failItem.setCode(500);
                failItem.setMessage("上链结果解析失败");
                failedItems.add(failItem);
            } catch (BizException e) {
                CertificateBatchAnchorFailItemVo failItem = new CertificateBatchAnchorFailItemVo();
                failItem.setCertificateId(certificateId);
                failItem.setCode(e.getCode());
                failItem.setMessage(e.getMessage());
                failedItems.add(failItem);
            } catch (Exception e) {
                CertificateBatchAnchorFailItemVo failItem = new CertificateBatchAnchorFailItemVo();
                failItem.setCertificateId(certificateId);
                failItem.setCode(500);
                failItem.setMessage("系统异常：" + e.getMessage());
                failedItems.add(failItem);
            }
        }

        CertificateBatchAnchorResultVo batchVo = new CertificateBatchAnchorResultVo();
        batchVo.setTotalCount(certificateIds.size());
        batchVo.setSuccessCount(successItems.size());
        batchVo.setFailedCount(failedItems.size());
        batchVo.setSuccessItems(successItems);
        batchVo.setFailedItems(failedItems);
        return Result.success("批量上链处理完成", batchVo);
    }

    @Override
    @MethodPurpose("查询证书当前上链结果")
    public CertificateAnchorResultVo queryAnchoredResult(Long certificateId) {
        if (certificateId == null) {
            return null;
        }
        Certificate certificate = certificateMapper.selectById(certificateId);
        if (certificate == null) {
            return null;
        }
        if (certificate.getBlockHeight() == null || !StringUtils.hasText(certificate.getTransactionHash())) {
            return null;
        }
        BlockchainRecord record = blockchainRecordMapper.selectByBlockHeight(certificate.getBlockHeight());
        if (record == null) {
            return null;
        }
        return toAnchorResult(certificate, record, true);
    }

    @MethodPurpose("尝试写入区块记录，处理并发时高度冲突")
    private BlockchainRecord tryCreateBlockRecord(Certificate certificate) {
        for (int retry = 0; retry < 5; retry++) {
            BlockchainRecord latestRecord = queryLatestBlock();
            long newHeight = latestRecord == null ? 1L : latestRecord.getBlockHeight() + 1;
            String previousHash = latestRecord == null ? GENESIS_PREVIOUS_HASH : latestRecord.getCurrentHash();
            long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            long nonce = RandomUtil.randomLong(100000L, Long.MAX_VALUE);
            String certificateHash = certificate.getHash();
            String merkleRoot = DigestUtil.sha256Hex(certificateHash);
            String payload = newHeight + "|" + previousHash + "|" + certificateHash + "|" + timestamp + "|" + nonce;
            String currentHash = DigestUtil.sha256Hex(payload);

            BlockchainRecord record = new BlockchainRecord();
            record.setBlockHeight(newHeight);
            record.setPreviousHash(previousHash);
            record.setCurrentHash(currentHash);
            record.setCertificateHash(certificateHash);
            record.setTimestamp(timestamp);
            record.setNonce(nonce);
            record.setMerkleRoot(merkleRoot);
            record.setBlockSize(payload.length());
            record.setTransactionCount(1);
            record.setCreatedTime(LocalDateTime.now());
            try {
                blockchainRecordMapper.insert(record);
                return record;
            } catch (DuplicateKeyException duplicateKeyException) {
                // 并发高度冲突时重试生成新区块高度
            }
        }
        throw new BizException(500, "证书上链失败，请稍后重试");
    }

    @MethodPurpose("查询最新区块记录")
    private BlockchainRecord queryLatestBlock() {
        return blockchainRecordMapper.selectLatestBlock();
    }

    @MethodPurpose("构建交易哈希")
    private String buildTransactionHash(Long certificateId, Long blockRecordId, Long timestamp) {
        return DigestUtil.sha256Hex("tx|" + certificateId + "|" + blockRecordId + "|" + timestamp + "|" + RandomUtil.randomString(8));
    }

    @MethodPurpose("组装上链结果对象")
    private CertificateAnchorResultVo toAnchorResult(Certificate certificate, BlockchainRecord record, boolean alreadyAnchored) {
        CertificateAnchorResultVo vo = new CertificateAnchorResultVo();
        vo.setCertificateId(certificate.getId());
        vo.setCertificateNumber(certificate.getCertificateNumber());
        vo.setBlockHeight(record.getBlockHeight());
        vo.setTransactionHash(certificate.getTransactionHash());
        vo.setPreviousHash(record.getPreviousHash());
        vo.setCurrentHash(record.getCurrentHash());
        vo.setAlreadyAnchored(alreadyAnchored);
        return vo;
    }

    @MethodPurpose("标准化批量证书ID列表并去重")
    private List<Long> normalizeCertificateIds(CertificateBatchAnchorDto dto) {
        if (dto == null || dto.getCertificateIds() == null || dto.getCertificateIds().isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> set = new LinkedHashSet<>();
        for (Long id : dto.getCertificateIds()) {
            if (id != null && id > 0) {
                set.add(id);
            }
        }
        return new ArrayList<>(set);
    }

    @MethodPurpose("从单条上链返回中提取上链结果对象")
    private CertificateAnchorResultVo unwrapAnchorResult(Result result, Long certificateId) {
        if (result == null || result.getCode() == null || result.getCode() != 200) {
            return null;
        }
        Object data = result.getData();
        if (data instanceof CertificateAnchorResultVo vo) {
            return vo;
        }
        return queryAnchoredResult(certificateId);
    }

    @MethodPurpose("按状态筛选证书上链列表")
    private void applyStatusFilter(LambdaQueryWrapper<Certificate> wrapper, String status) {
        if (!StringUtils.hasText(status)) {
            return;
        }
        switch (status) {
            case "ANCHORED" -> wrapper
                    .isNotNull(Certificate::getBlockHeight)
                    .and(w -> w.isNotNull(Certificate::getTransactionHash).ne(Certificate::getTransactionHash, ""));
            case "PENDING" -> wrapper
                    .and(w -> w.isNull(Certificate::getBlockHeight)
                            .or()
                            .isNull(Certificate::getTransactionHash)
                            .or()
                            .eq(Certificate::getTransactionHash, ""));
            case "FAILED" -> wrapper.apply("1 = 0");
            default -> wrapper.eq(Certificate::getStatus, status);
        }
    }

    @MethodPurpose("转换管理端证书上链分页视图")
    private CertificateAdminBlockchainPageVo toPageVo(Certificate certificate) {
        CertificateAdminBlockchainPageVo vo = new CertificateAdminBlockchainPageVo();
        vo.setCertificateId(certificate.getId());
        vo.setCertificateNumber(certificate.getCertificateNumber());
        vo.setStatus(certificate.getStatus());
        vo.setBlockHeight(certificate.getBlockHeight());
        vo.setTransactionHash(certificate.getTransactionHash());
        vo.setAnchored(certificate.getBlockHeight() != null && StringUtils.hasText(certificate.getTransactionHash()));
        return vo;
    }
}
