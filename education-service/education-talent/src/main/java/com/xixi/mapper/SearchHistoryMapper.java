package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索历史数据访问接口。
 */
@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

    /**
     * 分页查询企业搜索历史。
     */
    IPage<SearchHistory> selectHistoryPage(
            Page<SearchHistory> page,
            @Param("enterpriseId") Long enterpriseId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 删除指定时间前的搜索历史。
     */
    int deleteByEnterpriseAndBeforeTime(@Param("enterpriseId") Long enterpriseId, @Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 统计企业搜索总数。
     */
    Integer countTotalByEnterprise(@Param("enterpriseId") Long enterpriseId);

    /**
     * 按日期统计企业搜索数。
     */
    Integer countByEnterpriseAndDate(@Param("enterpriseId") Long enterpriseId, @Param("statDate") LocalDate statDate);

    /**
     * 查询有搜索行为的企业ID集合。
     */
    List<Long> selectDistinctEnterpriseIds();
}
