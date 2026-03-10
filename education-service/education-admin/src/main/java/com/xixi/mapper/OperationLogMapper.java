package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.OperationLog;
import com.xixi.pojo.vo.admin.AdminUserPageVo;
import com.xixi.pojo.vo.admin.MonitorApiOverviewVo;
import com.xixi.pojo.vo.admin.MonitorApiStatPageVo;
import com.xixi.pojo.vo.admin.MonitorDatabaseOverviewVo;
import com.xixi.pojo.vo.admin.MonitorSlowSqlPageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志 Mapper。
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    /**
     * 分页查询操作日志。
     */
    IPage<OperationLog> selectOperationPage(
            Page<OperationLog> page,
            @Param("userId") Long userId,
            @Param("userRole") String userRole,
            @Param("operationType") String operationType,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 导出操作日志列表。
     */
    List<OperationLog> selectForExport(
            @Param("userId") Long userId,
            @Param("userRole") String userRole,
            @Param("operationType") String operationType,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("maxRows") Integer maxRows
    );

    /**
     * 清理指定时间之前日志。
     */
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 分页聚合用户信息。
     */
    IPage<AdminUserPageVo> selectAdminUserPage(
            Page<AdminUserPageVo> page,
            @Param("keyword") String keyword,
            @Param("userRole") String userRole,
            @Param("status") Integer status
    );

    /**
     * 查询单个用户聚合信息。
     */
    AdminUserPageVo selectUserAggregate(@Param("userId") Long userId);

    /**
     * 查询用户最近操作日志。
     */
    List<OperationLog> selectRecentByUserId(@Param("userId") Long userId, @Param("limitSize") Integer limitSize);

    /**
     * 统计指定操作类型日志数量。
     */
    Integer countByOperationType(@Param("operationType") String operationType);

    /**
     * 统计指定操作类型和状态日志数量。
     */
    Integer countByOperationTypeAndStatus(@Param("operationType") String operationType, @Param("status") Integer status);

    /**
     * 查询最近一条成功的区块链操作日志。
     */
    OperationLog selectLatestSuccessBlockchainLog();

    /**
     * 数据库监控总览。
     */
    MonitorDatabaseOverviewVo selectDatabaseOverview(@Param("slowThresholdMs") Integer slowThresholdMs);

    /**
     * 慢 SQL 分页。
     */
    IPage<MonitorSlowSqlPageVo> selectSlowSqlPage(
            Page<MonitorSlowSqlPageVo> page,
            @Param("keyword") String keyword,
            @Param("slowThresholdMs") Integer slowThresholdMs
    );

    /**
     * API 调用总览。
     */
    MonitorApiOverviewVo selectApiOverview(@Param("startTime") LocalDateTime startTime);

    /**
     * API 调用统计分页。
     */
    IPage<MonitorApiStatPageVo> selectApiStatPage(
            Page<MonitorApiStatPageVo> page,
            @Param("keyword") String keyword
    );

    /**
     * API 调用趋势（最近 N 个周期）。
     */
    List<MonitorApiStatPageVo> selectApiTrend(@Param("limitSize") Integer limitSize);
}
