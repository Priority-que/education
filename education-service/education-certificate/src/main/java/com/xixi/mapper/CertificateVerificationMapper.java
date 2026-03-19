package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CertificateVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CertificateVerificationMapper extends BaseMapper<CertificateVerification> {

    /**
     * 分页查询验证历史。
     */
    IPage<CertificateVerification> selectVerifyHistoryPage(
            Page<CertificateVerification> page,
            @Param("isAdmin") Boolean isAdmin,
            @Param("verifierId") Long verifierId,
            @Param("verifierType") String verifierType,
            @Param("verificationResult") String verificationResult,
            @Param("verificationMethod") String verificationMethod
    );
}
