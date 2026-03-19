package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.DataStatistics;
import com.xixi.pojo.vo.admin.DashboardTrendPointVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 数据统计 Mapper。
 */
@Mapper
public interface DataStatisticsMapper extends BaseMapper<DataStatistics> {

    /**
     * 查询最新统计快照。
     */
    DataStatistics selectLatest();

    /**
     * 按指标查询趋势。
     */
    List<DashboardTrendPointVo> selectTrend(
            @Param("metricType") String metricType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 按统计日期查询快照。
     */
    DataStatistics selectByStatDate(@Param("statDate") LocalDate statDate);

    /**
     * 删除日期范围内统计快照。
     */
    int deleteByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
