package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.BlockchainRecord;
import com.xixi.entity.Certificate;
import com.xixi.exception.BizException;
import com.xixi.mapper.BlockchainRecordMapper;
import com.xixi.mapper.CertificateMapper;
import com.xixi.pojo.query.certificate.CertificateMyQuery;
import com.xixi.pojo.vo.certificate.CertificateDetailVo;
import com.xixi.pojo.vo.certificate.CertificateMyPageVo;
import com.xixi.service.StudentCertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 学生证书查询服务实现（5.1~5.3）。
 */
@Service
@RequiredArgsConstructor
public class StudentCertificateServiceImpl implements StudentCertificateService {
    private final CertificateMapper certificateMapper;
    private final BlockchainRecordMapper blockchainRecordMapper;

    @Override
    @MethodPurpose("5.1：分页查询当前学生证书列表")
    public IPage<CertificateMyPageVo> getMyCertificatePage(CertificateMyQuery query, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        CertificateMyQuery safeQuery = query == null ? new CertificateMyQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 10 : safeQuery.getPageSize();
        String status = StringUtils.hasText(safeQuery.getStatus()) ? safeQuery.getStatus().trim().toUpperCase() : null;
        String keyword = StringUtils.hasText(safeQuery.getKeyword()) ? safeQuery.getKeyword().trim() : null;
        Page<Certificate> entityPage = (Page<Certificate>) certificateMapper.selectMyCertificatePage(
                new Page<>(pageNum, pageSize),
                validStudentId,
                status,
                safeQuery.getCourseId(),
                keyword
        );
        Page<CertificateMyPageVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream()
                .map(item -> BeanUtil.copyProperties(item, CertificateMyPageVo.class))
                .toList());
        return voPage;
    }

    @Override
    @MethodPurpose("5.2：查询当前学生证书详情")
    public CertificateDetailVo getMyCertificateDetail(Long certificateId, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        Certificate certificate = requireOwnedCertificate(certificateId, validStudentId);
        return toDetailVo(certificate);
    }

    @Override
    @MethodPurpose("5.3：按证书编号查询当前学生证书详情")
    public CertificateDetailVo getMyCertificateByNumber(String certificateNumber, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        if (!StringUtils.hasText(certificateNumber)) {
            throw new BizException(400, "证书编号不能为空");
        }
        Certificate certificate = certificateMapper.selectByCertificateNumber(certificateNumber.trim());
        if (certificate == null) {
            throw new BizException(404, "证书不存在");
        }
        if (!Objects.equals(certificate.getStudentId(), validStudentId)) {
            throw new BizException(403, "无权限查看他人证书");
        }
        return toDetailVo(certificate);
    }

    @MethodPurpose("校验并返回当前学生ID")
    private Long requireStudentId(Long studentId) {
        if (studentId == null) {
            throw new BizException(401, "未登录或用户ID缺失");
        }
        return studentId;
    }

    @MethodPurpose("按证书ID查询并校验证书归属")
    private Certificate requireOwnedCertificate(Long certificateId, Long studentId) {
        if (certificateId == null) {
            throw new BizException(400, "证书ID不能为空");
        }
        Certificate certificate = certificateMapper.selectById(certificateId);
        if (certificate == null) {
            throw new BizException(404, "证书不存在");
        }
        if (!Objects.equals(certificate.getStudentId(), studentId)) {
            throw new BizException(403, "无权限查看他人证书");
        }
        return certificate;
    }

    @MethodPurpose("将证书实体转换为详情视图对象并补全区块链记录")
    private CertificateDetailVo toDetailVo(Certificate certificate) {
        CertificateDetailVo detailVo = BeanUtil.copyProperties(certificate, CertificateDetailVo.class);
        if (certificate.getBlockHeight() != null) {
            BlockchainRecord blockchainRecord = blockchainRecordMapper.selectByBlockHeight(certificate.getBlockHeight());
            detailVo.setBlockchainRecord(blockchainRecord);
        }
        return detailVo;
    }
}
