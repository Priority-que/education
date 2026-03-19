package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.AuditRecord;
import com.xixi.pojo.vo.admin.AuditStatRowVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {

    IPage<AuditRecord> selectAuditPage(
            Page<AuditRecord> page,
            @Param("auditType") String auditType,
            @Param("auditStatus") String auditStatus,
            @Param("targetName") String targetName,
            @Param("applicantName") String applicantName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    int updateAuditDecisionIfPending(
            @Param("id") Long id,
            @Param("auditorId") Long auditorId,
            @Param("auditorName") String auditorName,
            @Param("auditStatus") String auditStatus,
            @Param("auditOpinion") String auditOpinion,
            @Param("rejectReason") String rejectReason,
            @Param("auditTime") LocalDateTime auditTime,
            @Param("updatedTime") LocalDateTime updatedTime
    );

    List<AuditStatRowVo> selectAuditStatRows(@Param("auditType") String auditType);

    AuditRecord selectLatestByTypeAndTargetId(
            @Param("auditType") String auditType,
            @Param("targetId") Long targetId
    );

    IPage<AuditRecord> selectCertificateAuditPage(
            Page<AuditRecord> page,
            @Param("certificateNumber") String certificateNumber,
            @Param("status") String status
    );
}
