package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.EnterpriseVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 企业认证申请 Mapper。
 */
@Mapper
public interface EnterpriseVerificationMapper extends BaseMapper<EnterpriseVerification> {
    EnterpriseVerification selectCurrentByEnterprise(@Param("enterpriseId") Long enterpriseId);

    IPage<EnterpriseVerification> selectHistoryPage(
            Page<EnterpriseVerification> page,
            @Param("enterpriseId") Long enterpriseId
    );
}
