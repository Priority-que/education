package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.CertificateShare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface CertificateShareMapper extends BaseMapper<CertificateShare> {

    /**
     * 分页查询学生分享记录。
     */
    IPage<CertificateShare> selectMySharePage(
            Page<CertificateShare> page,
            @Param("studentId") Long studentId,
            @Param("certificateId") Long certificateId,
            @Param("isActive") Boolean isActive
    );

    /**
     * 根据分享令牌查询分享记录。
     */
    CertificateShare selectByShareToken(@Param("shareToken") String shareToken);

    /**
     * 增加分享浏览次数。
     */
    int increaseViewCount(
            @Param("shareId") Long shareId,
            @Param("updatedTime") LocalDateTime updatedTime
    );
}
