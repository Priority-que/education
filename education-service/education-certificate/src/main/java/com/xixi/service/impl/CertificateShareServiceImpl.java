package com.xixi.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.Certificate;
import com.xixi.entity.CertificateShare;
import com.xixi.exception.BizException;
import com.xixi.mapper.CertificateMapper;
import com.xixi.mapper.CertificateShareMapper;
import com.xixi.mq.CertificateShareChangedEventProducer;
import com.xixi.pojo.dto.certificate.CertificateShareCreateDto;
import com.xixi.pojo.query.certificate.CertificateShareMyQuery;
import com.xixi.pojo.vo.certificate.CertificatePublicShareVo;
import com.xixi.pojo.vo.certificate.CertificateShareVo;
import com.xixi.service.CertificateShareService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 证书分享服务实现（6.1~6.4）。
 */
@Service
@RequiredArgsConstructor
public class CertificateShareServiceImpl implements CertificateShareService {
    private static final String CERTIFICATE_STATUS_ISSUED = "ISSUED";
    private static final String PUBLIC_SHARE_PATH_PREFIX = "/certificate/share/public/";

    private final CertificateMapper certificateMapper;
    private final CertificateShareMapper certificateShareMapper;
    private final CertificateShareChangedEventProducer certificateShareChangedEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("6.1：学生创建证书分享链接")
    public Result createShare(CertificateShareCreateDto dto, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        validateCreateDto(dto);

        Certificate certificate = requireOwnedCertificate(dto.getCertificateId(), validStudentId);
        if (!CERTIFICATE_STATUS_ISSUED.equalsIgnoreCase(certificate.getStatus())) {
            throw new BizException(409, "仅已颁发证书允许创建分享");
        }

        String shareToken = RandomUtil.randomString(32);
        CertificateShare share = new CertificateShare();
        share.setCertificateId(certificate.getId());
        share.setStudentId(validStudentId);
        share.setShareToken(shareToken);
        share.setShareUrl(PUBLIC_SHARE_PATH_PREFIX + shareToken);
        share.setQrCodeUrl(PUBLIC_SHARE_PATH_PREFIX + shareToken);
        share.setExpiryTime(dto.getExpiryTime() == null ? LocalDateTime.now().plusDays(180) : dto.getExpiryTime());
        share.setViewCount(0);
        share.setIsActive(true);
        share.setCreatedTime(LocalDateTime.now());
        share.setUpdatedTime(LocalDateTime.now());
        certificateShareMapper.insert(share);

        certificateShareChangedEventProducer.publish(
                CertificateShareChangedEventProducer.EVENT_CREATE,
                share.getId(),
                share.getCertificateId(),
                share.getStudentId(),
                share.getShareToken(),
                share.getViewCount()
        );
        return Result.success("分享创建成功", Map.of(
                "shareId", share.getId(),
                "shareToken", share.getShareToken(),
                "shareUrl", share.getShareUrl()
        ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("6.2：学生失效自己的证书分享链接")
    public Result revokeShare(Long shareId, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        if (shareId == null) {
            throw new BizException(400, "分享ID不能为空");
        }

        CertificateShare share = certificateShareMapper.selectById(shareId);
        if (share == null) {
            throw new BizException(404, "分享记录不存在");
        }
        if (!Objects.equals(share.getStudentId(), validStudentId)) {
            throw new BizException(403, "无权限操作他人分享记录");
        }
        if (!Boolean.TRUE.equals(share.getIsActive())) {
            return Result.success("分享链接已失效");
        }

        share.setIsActive(false);
        share.setUpdatedTime(LocalDateTime.now());
        certificateShareMapper.updateById(share);

        certificateShareChangedEventProducer.publish(
                CertificateShareChangedEventProducer.EVENT_REVOKE,
                share.getId(),
                share.getCertificateId(),
                share.getStudentId(),
                share.getShareToken(),
                share.getViewCount()
        );
        return Result.success("分享链接已失效");
    }

    @Override
    @MethodPurpose("6.3：分页查询当前学生证书分享记录")
    public IPage<CertificateShareVo> getMySharePage(CertificateShareMyQuery query, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        CertificateShareMyQuery safeQuery = query == null ? new CertificateShareMyQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
        Boolean isActive = safeQuery.getIsActive() == null ? null : safeQuery.getIsActive() == 1;
        Page<CertificateShare> entityPage = (Page<CertificateShare>) certificateShareMapper.selectMySharePage(
                new Page<>(pageNum, pageSize),
                validStudentId,
                safeQuery.getCertificateId(),
                isActive
        );
        List<Long> certificateIds = entityPage.getRecords().stream()
                .map(CertificateShare::getCertificateId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Certificate> certificateMap = queryCertificateMap(certificateIds);
        Page<CertificateShareVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream()
                .map(item -> toShareVo(item, certificateMap.get(item.getCertificateId())))
                .toList());
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("6.4：公开访问证书分享链接并增加浏览计数")
    public CertificatePublicShareVo getPublicShareDetail(String shareToken) {
        if (!StringUtils.hasText(shareToken)) {
            throw new BizException(400, "shareToken不能为空");
        }
        String token = shareToken.trim();
        CertificateShare share = certificateShareMapper.selectByShareToken(token);
        if (share == null) {
            throw new BizException(404, "分享链接不存在");
        }
        if (!Boolean.TRUE.equals(share.getIsActive())) {
            throw new BizException(409, "分享链接已失效");
        }
        if (share.getExpiryTime() != null && share.getExpiryTime().isBefore(LocalDateTime.now())) {
            share.setIsActive(false);
            share.setUpdatedTime(LocalDateTime.now());
            certificateShareMapper.updateById(share);
            throw new BizException(409, "分享链接已过期");
        }

        Certificate certificate = certificateMapper.selectById(share.getCertificateId());
        if (certificate == null) {
            throw new BizException(404, "证书不存在");
        }

        int affected = certificateShareMapper.increaseViewCount(share.getId(), LocalDateTime.now());
        if (affected <= 0) {
            throw new BizException(409, "分享浏览计数更新失败");
        }
        share.setViewCount((share.getViewCount() == null ? 0 : share.getViewCount()) + 1);

        certificateShareChangedEventProducer.publish(
                CertificateShareChangedEventProducer.EVENT_PUBLIC_VIEW,
                share.getId(),
                share.getCertificateId(),
                share.getStudentId(),
                share.getShareToken(),
                share.getViewCount()
        );
        return toPublicShareVo(share, certificate);
    }

    @MethodPurpose("校验并返回当前学生ID")
    private Long requireStudentId(Long studentId) {
        if (studentId == null) {
            throw new BizException(401, "未登录或用户ID缺失");
        }
        return studentId;
    }

    @MethodPurpose("校验创建分享参数")
    private void validateCreateDto(CertificateShareCreateDto dto) {
        if (dto == null || dto.getCertificateId() == null || dto.getCertificateId() <= 0) {
            throw new BizException(400, "certificateId不能为空");
        }
        if (dto.getExpiryTime() != null && dto.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BizException(400, "expiryTime不能早于当前时间");
        }
    }

    @MethodPurpose("按证书ID查询并校验证书归属")
    private Certificate requireOwnedCertificate(Long certificateId, Long studentId) {
        Certificate certificate = certificateMapper.selectById(certificateId);
        if (certificate == null) {
            throw new BizException(404, "证书不存在");
        }
        if (!Objects.equals(certificate.getStudentId(), studentId)) {
            throw new BizException(403, "无权限操作他人证书");
        }
        return certificate;
    }

    @MethodPurpose("批量查询证书映射")
    private Map<Long, Certificate> queryCertificateMap(List<Long> certificateIds) {
        if (certificateIds == null || certificateIds.isEmpty()) {
            return Map.of();
        }
        return certificateMapper.selectByIds(certificateIds)
                .stream()
                .collect(Collectors.toMap(Certificate::getId, Function.identity(), (a, b) -> a));
    }

    @MethodPurpose("将分享记录转换为分页视图对象")
    private CertificateShareVo toShareVo(CertificateShare share, Certificate certificate) {
        CertificateShareVo vo = new CertificateShareVo();
        vo.setId(share.getId());
        vo.setCertificateId(share.getCertificateId());
        vo.setShareToken(share.getShareToken());
        vo.setShareUrl(share.getShareUrl());
        vo.setQrCodeUrl(share.getQrCodeUrl());
        vo.setExpiryTime(share.getExpiryTime());
        vo.setViewCount(share.getViewCount());
        vo.setIsActive(share.getIsActive());
        vo.setCreatedTime(share.getCreatedTime());
        vo.setUpdatedTime(share.getUpdatedTime());
        if (certificate != null) {
            vo.setCertificateNumber(certificate.getCertificateNumber());
            vo.setCertificateName(certificate.getCertificateName());
        }
        return vo;
    }

    @MethodPurpose("将分享记录与证书实体转换为公开访问视图")
    private CertificatePublicShareVo toPublicShareVo(CertificateShare share, Certificate certificate) {
        CertificatePublicShareVo vo = new CertificatePublicShareVo();
        vo.setShareId(share.getId());
        vo.setShareToken(share.getShareToken());
        vo.setShareUrl(share.getShareUrl());
        vo.setExpiryTime(share.getExpiryTime());
        vo.setViewCount(share.getViewCount());
        vo.setCertificateId(certificate.getId());
        vo.setCertificateNumber(certificate.getCertificateNumber());
        vo.setCertificateName(certificate.getCertificateName());
        vo.setIssuingAuthority(certificate.getIssuingAuthority());
        vo.setIssuingDate(certificate.getIssuingDate());
        vo.setExpiryDate(certificate.getExpiryDate());
        vo.setStatus(certificate.getStatus());
        vo.setThumbnailUrl(certificate.getThumbnailUrl());
        vo.setFileUrl(certificate.getFileUrl());
        vo.setHash(certificate.getHash());
        vo.setBlockHeight(certificate.getBlockHeight());
        vo.setTransactionHash(certificate.getTransactionHash());
        return vo;
    }
}
