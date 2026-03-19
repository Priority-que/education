package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.CertificateIssueRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 证书规则 Mapper。
 */
@Mapper
public interface CertificateIssueRuleMapper extends BaseMapper<CertificateIssueRule> {
    CertificateIssueRule selectByCourseId(@Param("courseId") Long courseId);

    CertificateIssueRule selectByIdAndTeacherId(@Param("id") Long id, @Param("teacherId") Long teacherId);

    int disableOtherEnabledRules(
            @Param("courseId") Long courseId,
            @Param("excludeId") Long excludeId,
            @Param("updatedTime") LocalDateTime updatedTime
    );

    int deleteByIdAndTeacherId(@Param("id") Long id, @Param("teacherId") Long teacherId);
}
