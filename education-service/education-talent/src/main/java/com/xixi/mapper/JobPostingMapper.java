package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.JobPosting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 岗位发布数据访问接口。
 */
@Mapper
public interface JobPostingMapper extends BaseMapper<JobPosting> {

    /**
     * 分页查询企业岗位列表。
     */
    IPage<JobPosting> selectJobPage(
            Page<JobPosting> page,
            @Param("enterpriseId") Long enterpriseId,
            @Param("status") String status,
            @Param("jobType") String jobType,
            @Param("keyword") String keyword
    );

    /**
     * 查询企业岗位详情。
     */
    JobPosting selectByEnterpriseAndId(@Param("enterpriseId") Long enterpriseId, @Param("id") Long id);

    /**
     * 查询学生端岗位广场分页。
     */
    IPage<Map<String, Object>> selectPublicJobPage(
            Page<Map<String, Object>> page,
            @Param("keyword") String keyword,
            @Param("jobType") String jobType,
            @Param("city") String city,
            @Param("educationRequirement") String educationRequirement,
            @Param("onlyOpen") Boolean onlyOpen,
            @Param("studentId") Long studentId
    );

    /**
     * 查询学生端岗位详情基础信息。
     */
    Map<String, Object> selectPublicJobDetail(@Param("jobId") Long jobId, @Param("studentId") Long studentId);

    /**
     * 查询企业端岗位详情及统计。
     */
    Map<String, Object> selectTalentJobDetail(@Param("jobId") Long jobId, @Param("enterpriseId") Long enterpriseId);

    /**
     * 更新岗位主体字段。
     */
    int updateJobByEnterprise(
            @Param("id") Long id,
            @Param("enterpriseId") Long enterpriseId,
            @Param("jobTitle") String jobTitle,
            @Param("jobType") String jobType,
            @Param("jobCategory") String jobCategory,
            @Param("workLocation") String workLocation,
            @Param("salaryRange") String salaryRange,
            @Param("experienceRequirement") String experienceRequirement,
            @Param("educationRequirement") String educationRequirement,
            @Param("jobDescription") String jobDescription,
            @Param("requirements") String requirements,
            @Param("benefits") String benefits,
            @Param("recruitmentNumber") Integer recruitmentNumber,
            @Param("applicationDeadline") LocalDate applicationDeadline,
            @Param("contactEmail") String contactEmail,
            @Param("contactPhone") String contactPhone,
            @Param("updatedTime") LocalDateTime updatedTime
    );

    /**
     * 更新岗位状态。
     */
    int updateStatusByEnterprise(
            @Param("id") Long id,
            @Param("enterpriseId") Long enterpriseId,
            @Param("status") String status,
            @Param("publishTime") LocalDateTime publishTime,
            @Param("updatedTime") LocalDateTime updatedTime
    );

    /**
     * 增加岗位浏览量。
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 增加岗位投递量。
     */
    int incrementApplyCount(@Param("id") Long id, @Param("delta") Integer delta);

    /**
     * 删除企业岗位。
     */
    int deleteByEnterpriseAndId(@Param("enterpriseId") Long enterpriseId, @Param("id") Long id);

    /**
     * 查询有岗位行为的企业ID集合。
     */
    List<Long> selectDistinctEnterpriseIds();
}
