package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.Certificate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CertificateMapper extends BaseMapper<Certificate> {

    /**
     * 分页查询学生证书列表。
     */
    IPage<Certificate> selectMyCertificatePage(
            Page<Certificate> page,
            @Param("studentId") Long studentId,
            @Param("status") String status,
            @Param("courseId") Long courseId,
            @Param("keyword") String keyword
    );

    /**
     * 根据证书编号查询证书。
     */
    Certificate selectByCertificateNumber(@Param("certificateNumber") String certificateNumber);

    /**
     * 统计学生在课程下的有效证书数量。
     */
    Long countByStudentAndCourseAndStatus(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("status") String status
    );

    /**
     * 分页查询教师已颁发证书。
     */
    IPage<Certificate> selectTeacherIssuedPage(
            Page<Certificate> page,
            @Param("teacherId") Long teacherId,
            @Param("courseId") Long courseId,
            @Param("status") String status
    );

    /**
     * 统计证书编号数量。
     */
    Long countByCertificateNumber(@Param("certificateNumber") String certificateNumber);

    /**
     * 查询最近一条证书哈希。
     */
    String selectLatestHash();

    /**
     * 批量按ID查询证书。
     */
    List<Certificate> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 查询学生证书列表（可按状态过滤）。
     */
    List<Certificate> selectByStudentIdAndStatus(
            @Param("studentId") Long studentId,
            @Param("status") String status
    );

    /**
     * 累加证书验证统计信息。
     */
    int increaseVerificationStats(
            @Param("certificateId") Long certificateId,
            @Param("updatedTime") LocalDateTime updatedTime,
            @Param("lastVerificationTime") LocalDateTime lastVerificationTime
    );
}
