package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.EnterpriseTalentStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 企业人才统计数据访问接口。
 */
@Mapper
public interface EnterpriseTalentStatisticsMapper extends BaseMapper<EnterpriseTalentStatistics> {

    /**
     * 查询企业指定日期统计快照。
     */
    EnterpriseTalentStatistics selectByEnterpriseAndDate(@Param("enterpriseId") Long enterpriseId, @Param("statDate") LocalDate statDate);

    /**
     * 查询企业最近一条统计快照。
     */
    EnterpriseTalentStatistics selectLatestByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    /**
     * 查询企业区间统计趋势。
     */
    List<EnterpriseTalentStatistics> selectTrendByEnterpriseAndDateRange(
            @Param("enterpriseId") Long enterpriseId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询统计表已有企业ID集合。
     */
    List<Long> selectDistinctEnterpriseIds();

    /**
     * 按增量更新企业当日统计指标。
     */
    int increaseDailyMetric(
            @Param("enterpriseId") Long enterpriseId,
            @Param("statDate") LocalDate statDate,
            @Param("totalFavoritesDelta") Integer totalFavoritesDelta,
            @Param("newFavoritesDelta") Integer newFavoritesDelta,
            @Param("totalContactsDelta") Integer totalContactsDelta,
            @Param("newContactsDelta") Integer newContactsDelta,
            @Param("totalInterviewsDelta") Integer totalInterviewsDelta,
            @Param("newInterviewsDelta") Integer newInterviewsDelta,
            @Param("totalHiresDelta") Integer totalHiresDelta,
            @Param("newHiresDelta") Integer newHiresDelta,
            @Param("totalSearchesDelta") Integer totalSearchesDelta
    );
}
