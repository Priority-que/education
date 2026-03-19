package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.TalentContact;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 联系人管理 Mapper。
 */
@Mapper
public interface TalentContactMapper extends BaseMapper<TalentContact> {
    IPage<Map<String, Object>> selectContactPage(
            Page<Map<String, Object>> page,
            @Param("enterpriseId") Long enterpriseId,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("studentId") Long studentId,
            @Param("applicationId") Long applicationId,
            @Param("jobId") Long jobId,
            @Param("sourceType") String sourceType
    );

    /**
     * 统计企业联系人总数。
     */
    Integer countTotalByEnterprise(@Param("enterpriseId") Long enterpriseId);

    TalentContact selectByEnterpriseAndId(@Param("enterpriseId") Long enterpriseId, @Param("id") Long id);

    int deleteByEnterpriseAndId(@Param("enterpriseId") Long enterpriseId, @Param("id") Long id);

    TalentContact selectByEnterpriseAndApplication(
            @Param("enterpriseId") Long enterpriseId,
            @Param("applicationId") Long applicationId
    );

    int updateFromApplication(
            @Param("id") Long id,
            @Param("jobId") Long jobId,
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("email") String email,
            @Param("position") String position,
            @Param("latestStatus") String latestStatus,
            @Param("lastContactTime") LocalDateTime lastContactTime,
            @Param("updatedTime") LocalDateTime updatedTime
    );
}
