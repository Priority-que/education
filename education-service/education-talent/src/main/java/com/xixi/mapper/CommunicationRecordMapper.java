package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CommunicationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 沟通记录数据访问接口。
 */
@Mapper
public interface CommunicationRecordMapper extends BaseMapper<CommunicationRecord> {

    /**
     * 分页查询企业沟通记录。
     */
    IPage<CommunicationRecord> selectCommunicationPage(
            Page<CommunicationRecord> page,
            @Param("enterpriseId") Long enterpriseId,
            @Param("applicationId") Long applicationId,
            @Param("jobId") Long jobId,
            @Param("studentId") Long studentId,
            @Param("communicationType") String communicationType,
            @Param("isRead") Boolean isRead
    );

    /**
     * 查询企业沟通记录详情。
     */
    CommunicationRecord selectByEnterpriseAndId(@Param("enterpriseId") Long enterpriseId, @Param("id") Long id);

    /**
     * 查询学生端沟通记录详情。
     */
    CommunicationRecord selectByStudentAndId(@Param("studentId") Long studentId, @Param("id") Long id);

    /**
     * 查询学生端沟通分页。
     */
    IPage<Map<String, Object>> selectStudentCommunicationPage(
            Page<Map<String, Object>> page,
            @Param("studentId") Long studentId,
            @Param("applicationId") Long applicationId,
            @Param("isRead") Boolean isRead
    );

    /**
     * 查询企业端扩展沟通分页。
     */
    IPage<Map<String, Object>> selectTalentCommunicationPage(
            Page<Map<String, Object>> page,
            @Param("enterpriseId") Long enterpriseId,
            @Param("applicationId") Long applicationId,
            @Param("jobId") Long jobId,
            @Param("studentId") Long studentId,
            @Param("communicationType") String communicationType,
            @Param("isRead") Boolean isRead
    );

    /**
     * 查询投递下的沟通列表。
     */
    List<Map<String, Object>> selectByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 标记沟通记录已读。
     */
    int markReadByEnterpriseAndId(
            @Param("enterpriseId") Long enterpriseId,
            @Param("id") Long id,
            @Param("readTime") LocalDateTime readTime
    );

    /**
     * 学生标记沟通已读。
     */
    int markReadByStudentAndId(
            @Param("studentId") Long studentId,
            @Param("id") Long id,
            @Param("readTime") LocalDateTime readTime
    );

    /**
     * 学生按投递批量标记沟通已读。
     */
    int markReadByStudentAndApplication(
            @Param("studentId") Long studentId,
            @Param("applicationId") Long applicationId,
            @Param("readTime") LocalDateTime readTime
    );

    /**
     * 学生确认沟通。
     */
    int confirmByStudentAndId(
            @Param("studentId") Long studentId,
            @Param("id") Long id,
            @Param("confirmRemark") String confirmRemark,
            @Param("confirmTime") LocalDateTime confirmTime,
            @Param("readTime") LocalDateTime readTime
    );

    /**
     * 统计企业沟通总数。
     */
    Integer countTotalByEnterprise(@Param("enterpriseId") Long enterpriseId);

    /**
     * 按日期统计企业新增沟通数。
     */
    Integer countCreatedByEnterpriseAndDate(@Param("enterpriseId") Long enterpriseId, @Param("statDate") LocalDate statDate);

    /**
     * 查询有沟通行为的企业ID集合。
     */
    List<Long> selectDistinctEnterpriseIds();
}
