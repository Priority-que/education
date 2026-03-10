package com.xixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.DataStatistics;
import com.xixi.exception.BizException;
import com.xixi.mapper.AuditRecordMapper;
import com.xixi.mapper.DataStatisticsMapper;
import com.xixi.mq.AdminDomainEventProducer;
import com.xixi.pojo.dto.admin.AdminInternalStatDailyDto;
import com.xixi.pojo.dto.admin.DashboardRebuildDto;
import com.xixi.pojo.query.admin.DashboardTrendQuery;
import com.xixi.pojo.vo.admin.AuditStatVo;
import com.xixi.pojo.vo.admin.DashboardDistributionVo;
import com.xixi.pojo.vo.admin.DashboardOverviewVo;
import com.xixi.pojo.vo.admin.DashboardTrendPointVo;
import com.xixi.service.AdminDashboardService;
import com.xixi.service.support.AdminOperationLogger;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理端看板服务实现。
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {
    private static final String BIZ_TYPE_DASHBOARD = "DASHBOARD";

    private final DataStatisticsMapper dataStatisticsMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final AdminDomainEventProducer adminDomainEventProducer;
    private final AdminOperationLogger adminOperationLogger;

    @Override
    @MethodPurpose("查询平台总览数据")
    public DashboardOverviewVo getOverview() {
        DataStatistics latest = dataStatisticsMapper.selectLatest();
        DashboardOverviewVo vo = new DashboardOverviewVo();
        if (latest == null) {
            return vo;
        }
        vo.setTotalUsers(latest.getTotalUsers());
        vo.setTotalStudents(latest.getTotalStudents());
        vo.setTotalTeachers(latest.getTotalTeachers());
        vo.setTotalEnterprises(latest.getTotalEnterprises());
        vo.setTotalCourses(latest.getTotalCourses());
        vo.setTotalCertificates(latest.getTotalCertificates());
        vo.setTotalResumes(latest.getTotalResumes());
        vo.setTotalJobs(latest.getTotalJobs());
        vo.setActiveUsers(latest.getActiveUsers());
        return vo;
    }

    @Override
    @MethodPurpose("按指标查询趋势数据")
    public List<DashboardTrendPointVo> getTrend(DashboardTrendQuery query) {
        DashboardTrendQuery safeQuery = query == null ? new DashboardTrendQuery() : query;
        LocalDate endDate = safeQuery.getEndDate() == null ? LocalDate.now() : safeQuery.getEndDate();
        LocalDate startDate = safeQuery.getStartDate() == null ? endDate.minusDays(30) : safeQuery.getStartDate();
        if (startDate.isAfter(endDate)) {
            throw new BizException(400, "startDate不能晚于endDate");
        }
        String metricType = normalizeMetricType(safeQuery.getMetricType());
        return dataStatisticsMapper.selectTrend(metricType, startDate, endDate);
    }

    @Override
    @MethodPurpose("查询看板分布统计")
    public DashboardDistributionVo getDistribution() {
        DataStatistics latest = dataStatisticsMapper.selectLatest();
        DashboardDistributionVo vo = new DashboardDistributionVo();
        if (latest == null) {
            vo.setRoleDistribution(Map.of());
            vo.setAuditTypeDistribution(List.of());
            return vo;
        }
        vo.setRoleDistribution(Map.of(
                "STUDENT", nullToZero(latest.getTotalStudents()),
                "TEACHER", nullToZero(latest.getTotalTeachers()),
                "ENTERPRISE", nullToZero(latest.getTotalEnterprises())
        ));
        List<AuditStatVo> auditStats = new ArrayList<>();
        auditRecordMapper.selectAuditStatRows(null).forEach(item -> {
            AuditStatVo statVo = new AuditStatVo();
            statVo.setAuditType(item.getAuditType());
            statVo.setTotalCount(item.getTotalCount());
            statVo.setPendingCount(item.getPendingCount());
            statVo.setApprovedCount(item.getApprovedCount());
            statVo.setRejectedCount(item.getRejectedCount());
            auditStats.add(statVo);
        });
        vo.setAuditTypeDistribution(auditStats);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("重建指定区间统计快照")
    public Result rebuildStatistics(DashboardRebuildDto dto, Long adminId) {
        if (adminId == null) {
            throw new BizException(401, "未登录或管理员ID缺失");
        }
        if (dto == null || dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new BizException(400, "startDate和endDate不能为空");
        }
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BizException(400, "startDate不能晚于endDate");
        }
        if (dto.getEndDate().isAfter(LocalDate.now())) {
            throw new BizException(400, "endDate不能晚于当前日期");
        }
        dataStatisticsMapper.deleteByDateRange(dto.getStartDate(), dto.getEndDate());

        DataStatistics latest = dataStatisticsMapper.selectLatest();
        LocalDate cursor = dto.getStartDate();
        int rebuildCount = 0;
        while (!cursor.isAfter(dto.getEndDate())) {
            DataStatistics stat = new DataStatistics();
            stat.setStatDate(cursor);
            stat.setTotalUsers(latest == null ? 0 : nullToZero(latest.getTotalUsers()));
            stat.setNewUsers(0);
            stat.setActiveUsers(latest == null ? 0 : nullToZero(latest.getActiveUsers()));
            stat.setTotalStudents(latest == null ? 0 : nullToZero(latest.getTotalStudents()));
            stat.setTotalTeachers(latest == null ? 0 : nullToZero(latest.getTotalTeachers()));
            stat.setTotalEnterprises(latest == null ? 0 : nullToZero(latest.getTotalEnterprises()));
            stat.setTotalCourses(latest == null ? 0 : nullToZero(latest.getTotalCourses()));
            stat.setNewCourses(0);
            stat.setTotalCertificates(latest == null ? 0 : nullToZero(latest.getTotalCertificates()));
            stat.setNewCertificates(0);
            stat.setTotalResumes(latest == null ? 0 : nullToZero(latest.getTotalResumes()));
            stat.setTotalJobs(latest == null ? 0 : nullToZero(latest.getTotalJobs()));
            stat.setTotalStudyTime(latest == null ? 0L : (latest.getTotalStudyTime() == null ? 0L : latest.getTotalStudyTime()));
            stat.setCreatedTime(LocalDateTime.now());
            dataStatisticsMapper.insert(stat);
            rebuildCount++;
            cursor = cursor.plusDays(1);
        }

        adminDomainEventProducer.publish(
                "REBUILD",
                BIZ_TYPE_DASHBOARD,
                null,
                JSONUtil.toJsonStr(Map.of(
                        "startDate", dto.getStartDate().toString(),
                        "endDate", dto.getEndDate().toString(),
                        "rebuildCount", rebuildCount
                )),
                adminId
        );
        adminOperationLogger.log(
                adminId,
                "管理员" + adminId,
                "ADMIN",
                "DASHBOARD_REBUILD",
                "重建统计快照",
                "POST",
                "/admin/dashboard/rebuild",
                JSONUtil.toJsonStr(dto),
                JSONUtil.toJsonStr(Map.of("rebuildCount", rebuildCount)),
                null,
                null,
                0,
                Boolean.TRUE,
                null
        );
        return Result.success("统计重建成功", Map.of("rebuildCount", rebuildCount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("内部上报日统计快照")
    public Result reportDailyStatistics(AdminInternalStatDailyDto dto) {
        if (dto == null || dto.getStatDate() == null) {
            throw new BizException(400, "statDate不能为空");
        }
        DataStatistics exists = dataStatisticsMapper.selectByStatDate(dto.getStatDate());
        if (exists == null) {
            DataStatistics insert = new DataStatistics();
            insert.setStatDate(dto.getStatDate());
            fillStatisticFields(insert, dto);
            insert.setCreatedTime(LocalDateTime.now());
            dataStatisticsMapper.insert(insert);
        } else {
            fillStatisticFields(exists, dto);
            dataStatisticsMapper.updateById(exists);
        }
        adminDomainEventProducer.publish(
                "REPORT",
                BIZ_TYPE_DASHBOARD,
                null,
                JSONUtil.toJsonStr(Map.of("statDate", dto.getStatDate().toString())),
                null
        );
        return Result.success("统计快照上报成功");
    }

    @MethodPurpose("填充统计字段")
    private void fillStatisticFields(DataStatistics target, AdminInternalStatDailyDto source) {
        target.setTotalUsers(nullToZero(source.getTotalUsers()));
        target.setNewUsers(nullToZero(source.getNewUsers()));
        target.setActiveUsers(nullToZero(source.getActiveUsers()));
        target.setTotalStudents(nullToZero(source.getTotalStudents()));
        target.setTotalTeachers(nullToZero(source.getTotalTeachers()));
        target.setTotalEnterprises(nullToZero(source.getTotalEnterprises()));
        target.setTotalCourses(nullToZero(source.getTotalCourses()));
        target.setNewCourses(nullToZero(source.getNewCourses()));
        target.setTotalCertificates(nullToZero(source.getTotalCertificates()));
        target.setNewCertificates(nullToZero(source.getNewCertificates()));
        target.setTotalResumes(nullToZero(source.getTotalResumes()));
        target.setTotalJobs(nullToZero(source.getTotalJobs()));
        target.setTotalStudyTime(source.getTotalStudyTime() == null ? 0L : source.getTotalStudyTime());
    }

    @MethodPurpose("标准化指标类型")
    private String normalizeMetricType(String metricType) {
        if (!StringUtils.hasText(metricType)) {
            return "TOTAL_USERS";
        }
        String normalized = metricType.trim().toUpperCase();
        if (!List.of("TOTAL_USERS", "ACTIVE_USERS", "TOTAL_COURSES", "TOTAL_CERTIFICATES", "TOTAL_RESUMES", "TOTAL_JOBS")
                .contains(normalized)) {
            throw new BizException(400, "metricType非法");
        }
        return normalized;
    }

    @MethodPurpose("空值转0")
    private Integer nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
