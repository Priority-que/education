package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.entity.SystemMonitor;
import com.xixi.pojo.vo.admin.MonitorHealthVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 系统监控 Mapper。
 */
@Mapper
public interface SystemMonitorMapper extends BaseMapper<SystemMonitor> {

    /**
     * 查询健康总览。
     */
    MonitorHealthVo selectHealthOverview();

    /**
     * 分页查询服务最新监控。
     */
    IPage<SystemMonitor> selectServiceLatestPage(
            Page<SystemMonitor> page,
            @Param("serviceName") String serviceName,
            @Param("status") String status
    );

    /**
     * 查询单服务监控明细。
     */
    List<SystemMonitor> selectServiceDetail(
            @Param("serviceName") String serviceName,
            @Param("limitSize") Integer limitSize
    );

    /**
     * 分页查询告警记录。
     */
    IPage<SystemMonitor> selectAlertPage(
            Page<SystemMonitor> page,
            @Param("minCpuUsage") BigDecimal minCpuUsage,
            @Param("minMemoryUsage") BigDecimal minMemoryUsage,
            @Param("minErrorRate") BigDecimal minErrorRate,
            @Param("status") String status
    );
}
