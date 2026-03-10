package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.TalentFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 人才收藏数据访问接口。
 */
@Mapper
public interface TalentFavoriteMapper extends BaseMapper<TalentFavorite> {

    /**
     * 分页查询企业收藏列表。
     */
    IPage<TalentFavorite> selectFavoritePage(
            Page<TalentFavorite> page,
            @Param("enterpriseId") Long enterpriseId,
            @Param("status") String status,
            @Param("tagName") String tagName,
            @Param("keyword") String keyword
    );

    /**
     * 查询企业收藏详情。
     */
    TalentFavorite selectByEnterpriseAndId(@Param("enterpriseId") Long enterpriseId, @Param("id") Long id);

    /**
     * 统计企业按简历收藏数量。
     */
    Long countByEnterpriseAndStudent(@Param("enterpriseId") Long enterpriseId, @Param("studentId") Long studentId);

    /**
     * 更新收藏内容字段。
     */
    int updateFavoriteContent(
            @Param("id") Long id,
            @Param("enterpriseId") Long enterpriseId,
            @Param("tags") String tags,
            @Param("rating") Integer rating,
            @Param("notes") String notes,
            @Param("updatedTime") LocalDateTime updatedTime
    );

    /**
     * 更新收藏状态字段。
     */
    int updateFavoriteStatus(
            @Param("id") Long id,
            @Param("enterpriseId") Long enterpriseId,
            @Param("status") String status,
            @Param("updatedTime") LocalDateTime updatedTime
    );

    /**
     * 删除企业收藏记录。
     */
    int deleteByEnterpriseAndId(@Param("enterpriseId") Long enterpriseId, @Param("id") Long id);

    /**
     * 统计企业收藏总数。
     */
    Integer countTotalByEnterprise(@Param("enterpriseId") Long enterpriseId);

    /**
     * 统计企业某状态收藏总数。
     */
    Integer countByEnterpriseAndStatus(@Param("enterpriseId") Long enterpriseId, @Param("status") String status);

    /**
     * 按日期统计新增收藏数。
     */
    Integer countCreatedByEnterpriseAndDate(@Param("enterpriseId") Long enterpriseId, @Param("statDate") LocalDate statDate);

    /**
     * 按日期统计指定状态变更数。
     */
    Integer countStatusUpdatedByEnterpriseAndDate(
            @Param("enterpriseId") Long enterpriseId,
            @Param("status") String status,
            @Param("statDate") LocalDate statDate
    );

    /**
     * 查询有收藏行为的企业ID集合。
     */
    List<Long> selectDistinctEnterpriseIds();
}
