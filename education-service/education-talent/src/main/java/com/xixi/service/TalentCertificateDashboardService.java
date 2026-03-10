package com.xixi.service;

import com.xixi.annotation.MethodPurpose;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.EnterpriseTalentStatistics;
import com.xixi.exception.BizException;
import com.xixi.mapper.CommunicationRecordMapper;
import com.xixi.mapper.EnterpriseTalentStatisticsMapper;
import com.xixi.mapper.SearchHistoryMapper;
import com.xixi.mapper.TalentFavoriteMapper;
import com.xixi.mq.TalentDomainEventProducer;
import com.xixi.openfeign.certificate.EducationCertificateVerifyClient;
import com.xixi.pojo.dto.talent.CertificateVerifyBatchDto;
import com.xixi.pojo.dto.talent.CertificateVerifyByNumberDto;
import com.xixi.pojo.dto.talent.CertificateVerifyByQrcodeDto;
import com.xixi.pojo.dto.talent.TalentDashboardTrendQueryDto;
import com.xixi.pojo.vo.talent.TalentDashboardOverviewVo;
import com.xixi.pojo.vo.talent.TalentDashboardTrendPointVo;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 证书验证编排与看板统计服务。
 */
@Service
@RequiredArgsConstructor
public class TalentCertificateDashboardService {
    private static final String METRIC_NEW_FAVORITES = "NEW_FAVORITES";
    private static final String METRIC_NEW_CONTACTS = "NEW_CONTACTS";
    private static final String METRIC_NEW_INTERVIEWS = "NEW_INTERVIEWS";
    private static final String METRIC_NEW_HIRES = "NEW_HIRES";
    private static final String METRIC_TOTAL_SEARCHES = "TOTAL_SEARCHES";

    private final EnterpriseIdentityService enterpriseIdentityService;
    private final EducationCertificateVerifyClient educationCertificateVerifyClient;
    private final EnterpriseTalentStatisticsMapper enterpriseTalentStatisticsMapper;
    private final TalentFavoriteMapper talentFavoriteMapper;
    private final CommunicationRecordMapper communicationRecordMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final TalentDomainEventProducer talentDomainEventProducer;

    @MethodPurpose("编排证书服务：按编号验证证书")
    public Object verifyByNumber(CertificateVerifyByNumberDto dto, Long userId, Integer role) {
        verifyLoginContext(userId, role);
        Result result = educationCertificateVerifyClient.verifyByNumber(dto, String.valueOf(userId), String.valueOf(role));
        return requireRemoteSuccess(result, "证书编号验证失败");
    }

    @MethodPurpose("编排证书服务：按二维码验证证书")
    public Object verifyByQrcode(CertificateVerifyByQrcodeDto dto, Long userId, Integer role) {
        verifyLoginContext(userId, role);
        Result result = educationCertificateVerifyClient.verifyByQrcode(dto, String.valueOf(userId), String.valueOf(role));
        return requireRemoteSuccess(result, "二维码验证失败");
    }

    @MethodPurpose("编排证书服务：批量验证证书")
    public Object verifyBatch(CertificateVerifyBatchDto dto, Long userId, Integer role) {
        verifyLoginContext(userId, role);
        Result result = educationCertificateVerifyClient.verifyBatch(dto, String.valueOf(userId), String.valueOf(role));
        Object data = requireRemoteSuccess(result, "批量验证失败");
        Long enterpriseId = role == RoleConstants.ENTERPRISE ? enterpriseIdentityService.requireEnterpriseId(userId) : null;
        if (enterpriseId != null) {
            talentDomainEventProducer.publish(
                    TalentDomainEventProducer.EVENT_SEARCH_EXECUTED,
                    enterpriseId,
                    null,
                    Map.of("source", "CERTIFICATE_VERIFY_BATCH")
            );
        }
        return data;
    }

    @MethodPurpose("编排证书服务：查询验证历史")
    public Object getVerifyHistory(Long pageNum, Long pageSize, String verificationResult, String verificationMethod, Long userId, Integer role) {
        verifyLoginContext(userId, role);
        long safePageNum = pageNum == null || pageNum <= 0 ? 1 : pageNum;
        long safePageSize = pageSize == null || pageSize <= 0 ? 10 : Math.min(pageSize, 100);
        Result result = educationCertificateVerifyClient.getVerifyHistory(
                safePageNum,
                safePageSize,
                trimToNull(verificationResult),
                trimToNull(verificationMethod),
                String.valueOf(userId),
                String.valueOf(role)
        );
        return requireRemoteSuccess(result, "验证历史查询失败");
    }

    @MethodPurpose("编排证书服务：查询验证报告详情")
    public Object getVerifyReport(Long verificationId, Long userId, Integer role) {
        verifyLoginContext(userId, role);
        if (verificationId == null) {
            throw new BizException(400, "verificationId不能为空");
        }
        Result result = educationCertificateVerifyClient.getVerifyReport(
                verificationId,
                String.valueOf(userId),
                String.valueOf(role)
        );
        return requireRemoteSuccess(result, "验证报告查询失败");
    }

    @MethodPurpose("查询企业看板总览数据")
    public TalentDashboardOverviewVo getDashboardOverview(Long userId, Integer role, Long enterpriseIdParam) {
        Long enterpriseId = resolveDashboardEnterpriseId(userId, role, enterpriseIdParam);
        EnterpriseTalentStatistics latest = enterpriseTalentStatisticsMapper.selectLatestByEnterpriseId(enterpriseId);

        TalentDashboardOverviewVo vo = new TalentDashboardOverviewVo();
        vo.setEnterpriseId(enterpriseId);
        if (latest != null) {
            vo.setStatDate(latest.getStatDate());
            vo.setTotalFavorites(nullToZero(latest.getTotalFavorites()));
            vo.setTotalContacts(nullToZero(latest.getTotalContacts()));
            vo.setTotalInterviews(nullToZero(latest.getTotalInterviews()));
            vo.setTotalHires(nullToZero(latest.getTotalHires()));
            vo.setTotalSearches(nullToZero(latest.getTotalSearches()));
            return vo;
        }

        vo.setStatDate(LocalDate.now());
        vo.setTotalFavorites(nullToZero(talentFavoriteMapper.countTotalByEnterprise(enterpriseId)));
        vo.setTotalContacts(nullToZero(communicationRecordMapper.countTotalByEnterprise(enterpriseId)));
        vo.setTotalInterviews(nullToZero(talentFavoriteMapper.countByEnterpriseAndStatus(enterpriseId, "INTERVIEWED")));
        vo.setTotalHires(nullToZero(talentFavoriteMapper.countByEnterpriseAndStatus(enterpriseId, "HIRED")));
        vo.setTotalSearches(nullToZero(searchHistoryMapper.countTotalByEnterprise(enterpriseId)));
        return vo;
    }

    @MethodPurpose("查询企业看板趋势数据")
    public List<TalentDashboardTrendPointVo> getDashboardTrend(TalentDashboardTrendQueryDto query, Long userId, Integer role) {
        TalentDashboardTrendQueryDto safeQuery = query == null ? new TalentDashboardTrendQueryDto() : query;
        Long enterpriseId = resolveDashboardEnterpriseId(userId, role, safeQuery.getEnterpriseId());
        LocalDate endDate = safeQuery.getEndDate() == null ? LocalDate.now() : safeQuery.getEndDate();
        LocalDate startDate = safeQuery.getStartDate() == null ? endDate.minusDays(6) : safeQuery.getStartDate();
        if (startDate.isAfter(endDate)) {
            throw new BizException(400, "startDate不能大于endDate");
        }
        String metricType = normalizeMetricType(safeQuery.getMetricType());
        List<EnterpriseTalentStatistics> stats = enterpriseTalentStatisticsMapper.selectTrendByEnterpriseAndDateRange(
                enterpriseId, startDate, endDate
        );

        Map<LocalDate, EnterpriseTalentStatistics> statMap = new HashMap<>();
        for (EnterpriseTalentStatistics stat : stats) {
            statMap.put(stat.getStatDate(), stat);
        }

        List<TalentDashboardTrendPointVo> trend = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            EnterpriseTalentStatistics stat = statMap.get(cursor);
            TalentDashboardTrendPointVo point = new TalentDashboardTrendPointVo();
            point.setStatDate(cursor);
            point.setMetricType(metricType);
            point.setMetricValue(extractMetricValue(stat, metricType));
            trend.add(point);
            cursor = cursor.plusDays(1);
        }
        return trend;
    }

    @MethodPurpose("解析看板查询目标企业ID")
    private Long resolveDashboardEnterpriseId(Long userId, Integer role, Long enterpriseIdParam) {
        if (role == null) {
            throw new BizException(401, "未登录或角色缺失");
        }
        if (role == RoleConstants.ENTERPRISE) {
            return enterpriseIdentityService.requireEnterpriseId(userId);
        }
        if (role == RoleConstants.ADMIN) {
            if (enterpriseIdParam == null) {
                throw new BizException(400, "管理员查询看板需传enterpriseId");
            }
            return enterpriseIdParam;
        }
        throw new BizException(403, "当前角色无权限访问看板");
    }

    @MethodPurpose("校验证书验证调用登录上下文")
    private void verifyLoginContext(Long userId, Integer role) {
        if (userId == null || role == null) {
            throw new BizException(401, "未登录或角色缺失");
        }
        if (role != RoleConstants.ENTERPRISE && role != RoleConstants.ADMIN) {
            throw new BizException(403, "当前角色无权限调用证书验证");
        }
    }

    @MethodPurpose("校验远程调用成功并返回数据")
    private Object requireRemoteSuccess(Result result, String errorPrefix) {
        if (result == null || result.getCode() == null || result.getCode() != 200) {
            throw new BizException(result == null ? 500 : result.getCode(),
                    result == null ? errorPrefix : errorPrefix + "：" + result.getMessage());
        }
        return result.getData();
    }

    @MethodPurpose("指标类型标准化")
    private String normalizeMetricType(String metricType) {
        if (!StringUtils.hasText(metricType)) {
            return METRIC_TOTAL_SEARCHES;
        }
        String normalized = metricType.trim().toUpperCase();
        if (METRIC_NEW_FAVORITES.equals(normalized)
                || METRIC_NEW_CONTACTS.equals(normalized)
                || METRIC_NEW_INTERVIEWS.equals(normalized)
                || METRIC_NEW_HIRES.equals(normalized)
                || METRIC_TOTAL_SEARCHES.equals(normalized)) {
            return normalized;
        }
        throw new BizException(400, "metricType不合法");
    }

    @MethodPurpose("按指标类型提取趋势值")
    private Integer extractMetricValue(EnterpriseTalentStatistics stat, String metricType) {
        if (stat == null) {
            return 0;
        }
        if (METRIC_NEW_FAVORITES.equals(metricType)) {
            return nullToZero(stat.getNewFavorites());
        }
        if (METRIC_NEW_CONTACTS.equals(metricType)) {
            return nullToZero(stat.getNewContacts());
        }
        if (METRIC_NEW_INTERVIEWS.equals(metricType)) {
            return nullToZero(stat.getNewInterviews());
        }
        if (METRIC_NEW_HIRES.equals(metricType)) {
            return nullToZero(stat.getNewHires());
        }
        return nullToZero(stat.getTotalSearches());
    }

    @MethodPurpose("空值转0")
    private Integer nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    @MethodPurpose("字符串去空并返回null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
