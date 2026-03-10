package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.JobApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 岗位投递记录 Mapper。
 */
@Mapper
public interface JobApplicationMapper extends BaseMapper<JobApplication> {
    JobApplication selectLatestByJobAndStudent(@Param("jobId") Long jobId, @Param("studentId") Long studentId);

    JobApplication selectByStudentAndId(@Param("studentId") Long studentId, @Param("id") Long id);

    JobApplication selectByEnterpriseAndId(@Param("enterpriseId") Long enterpriseId, @Param("id") Long id);

    IPage<Map<String, Object>> selectStudentApplicationPage(
            Page<Map<String, Object>> page,
            @Param("studentId") Long studentId,
            @Param("status") String status,
            @Param("keyword") String keyword
    );

    IPage<Map<String, Object>> selectTalentApplicationPage(
            Page<Map<String, Object>> page,
            @Param("enterpriseId") Long enterpriseId,
            @Param("jobId") Long jobId,
            @Param("status") String status,
            @Param("keyword") String keyword
    );

    List<Map<String, Object>> selectStudentResumeOptionList(@Param("studentId") Long studentId);

    Map<String, Object> selectResumeSnapshotSource(@Param("resumeId") Long resumeId, @Param("studentId") Long studentId);

    List<Map<String, Object>> selectResumeCertificateSnapshotList(@Param("resumeId") Long resumeId, @Param("studentId") Long studentId);

    Map<String, Object> selectTalentApplicationStudentInfo(@Param("applicationId") Long applicationId, @Param("enterpriseId") Long enterpriseId);

    List<Map<String, Object>> selectJobStatusStats(@Param("jobId") Long jobId, @Param("enterpriseId") Long enterpriseId);

    int updateStudentReadFlag(@Param("id") Long id, @Param("studentId") Long studentId, @Param("readByStudent") Boolean readByStudent);

    int updateEnterpriseReadFlag(@Param("id") Long id, @Param("enterpriseId") Long enterpriseId, @Param("readByEnterprise") Boolean readByEnterprise);

    int updateApplicationStatus(
            @Param("id") Long id,
            @Param("enterpriseId") Long enterpriseId,
            @Param("status") String status,
            @Param("remark") String remark,
            @Param("readByStudent") Boolean readByStudent,
            @Param("updatedTime") LocalDateTime updatedTime
    );

    int updateApplicationAfterCommunication(
            @Param("id") Long id,
            @Param("latestCommunicationId") Long latestCommunicationId,
            @Param("latestCommunicationType") String latestCommunicationType,
            @Param("latestCommunicationTime") LocalDateTime latestCommunicationTime,
            @Param("readByStudent") Boolean readByStudent,
            @Param("readByEnterprise") Boolean readByEnterprise,
            @Param("updatedTime") LocalDateTime updatedTime
    );

    int updateApplicationWithdraw(
            @Param("id") Long id,
            @Param("studentId") Long studentId,
            @Param("status") String status,
            @Param("readByEnterprise") Boolean readByEnterprise,
            @Param("updatedTime") LocalDateTime updatedTime
    );

    int countActiveByJobAndStudent(@Param("jobId") Long jobId, @Param("studentId") Long studentId);

    Long selectEnterpriseUserIdByJobId(@Param("jobId") Long jobId);

    Long selectStudentUserIdByStudentId(@Param("studentId") Long studentId);

    Long selectStudentIdByUserId(@Param("userId") Long userId);
}
