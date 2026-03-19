package com.xixi.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.SearchHistory;
import com.xixi.exception.BizException;
import com.xixi.mapper.SearchHistoryMapper;
import com.xixi.mq.TalentDomainEventProducer;
import com.xixi.openfeign.resume.EducationResumeTalentClient;
import com.xixi.pojo.dto.talent.ResumeCompareDto;
import com.xixi.pojo.dto.talent.SearchHistoryPageQueryDto;
import com.xixi.pojo.dto.talent.TalentRecommendDto;
import com.xixi.pojo.dto.talent.TalentSearchQueryDto;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 人才搜索与简历发现服务。
 */
@Service
@RequiredArgsConstructor
public class TalentSearchService {
    private static final int MAX_EXPORT_SIZE = 200;

    private final EnterpriseIdentityService enterpriseIdentityService;
    private final SearchHistoryMapper searchHistoryMapper;
    private final EducationResumeTalentClient educationResumeTalentClient;
    private final TalentDomainEventProducer talentDomainEventProducer;

    @MethodPurpose("执行人才搜索分页并记录企业搜索历史")
    @Transactional(rollbackFor = Exception.class)
    public Object searchPage(TalentSearchQueryDto query, Long userId, String clientIp) {
        TalentSearchQueryDto safeQuery = query == null ? new TalentSearchQueryDto() : query;
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        String keyword = buildKeyword(safeQuery.getKeyword(), safeQuery.getSkillTag());
        Integer pageNum = normalizePageNum(safeQuery.getPageNum());
        Integer pageSize = normalizePageSize(safeQuery.getPageSize());

        Result result = educationResumeTalentClient.getInternalPublicStudentPage(
                pageNum,
                pageSize,
                keyword,
                null,
                trimToNull(safeQuery.getEducationRequirement()),
                String.valueOf(userId),
                String.valueOf(RoleConstants.ENTERPRISE)
        );
        Object pageData = requireRemoteSuccess(result, "公开简历分页查询失败");

        SearchHistory history = new SearchHistory();
        history.setEnterpriseId(enterpriseId);
        history.setSearchKeyword(keyword);
        history.setSearchFilters(JSONUtil.toJsonStr(safeQuery));
        history.setResultCount(extractTotalCount(pageData));
        history.setSearchTime(LocalDateTime.now());
        history.setIpAddress(trimToNull(clientIp));
        history.setCreatedTime(LocalDateTime.now());
        searchHistoryMapper.insert(history);

        Map<String, Object> payload = new HashMap<>();
        payload.put("keyword", keyword);
        payload.put("resultCount", history.getResultCount());
        talentDomainEventProducer.publish(
                TalentDomainEventProducer.EVENT_SEARCH_EXECUTED,
                enterpriseId,
                history.getId(),
                payload
        );
        return pageData;
    }

    @MethodPurpose("地图搜索：补充经纬度与距离信息并按半径筛选")
    public Object searchMap(TalentSearchQueryDto query, Long userId, String clientIp) {
        TalentSearchQueryDto safeQuery = query == null ? new TalentSearchQueryDto() : query;
        Object pageData = searchPage(safeQuery, userId, clientIp);
        if (!(pageData instanceof Map<?, ?> dataMap)) {
            return pageData;
        }

        Map<String, Object> result = new LinkedHashMap<>(toRecordMap(dataMap));
        Object recordsObj = result.get("records");
        if (!(recordsObj instanceof List<?> rawRecords)) {
            return result;
        }

        BigDecimal centerLon = safeQuery.getLongitude();
        BigDecimal centerLat = safeQuery.getLatitude();
        Integer radiusKm = safeQuery.getRadiusKm();

        List<Map<String, Object>> mapped = new ArrayList<>();
        for (Object item : rawRecords) {
            Map<String, Object> map = new LinkedHashMap<>(toRecordMap(item));
            BigDecimal lon = parseDecimal(map.get("longitude"));
            BigDecimal lat = parseDecimal(map.get("latitude"));
            BigDecimal distanceKm = null;
            if (lon != null && lat != null && centerLon != null && centerLat != null) {
                distanceKm = calculateDistanceKm(centerLon, centerLat, lon, lat);
            }
            if (radiusKm != null && radiusKm > 0 && distanceKm != null
                    && distanceKm.compareTo(BigDecimal.valueOf(radiusKm)) > 0) {
                continue;
            }
            map.put("longitude", lon);
            map.put("latitude", lat);
            map.put("distanceKm", distanceKm);
            mapped.add(map);
        }
        result.put("records", mapped);
        result.put("total", mapped.size());
        return result;
    }

    @MethodPurpose("基于推荐参数调用公开简历检索并返回推荐结果")
    public Object recommend(TalentRecommendDto dto, Long userId, String clientIp) {
        TalentRecommendDto safeDto = dto == null ? new TalentRecommendDto() : dto;
        TalentSearchQueryDto query = new TalentSearchQueryDto();
        query.setPageNum(1);
        query.setPageSize(safeDto.getPageSize() == null ? 10 : safeDto.getPageSize());
        query.setKeyword(safeDto.getKeyword());
        query.setSkillTag(safeDto.getSkillTag());
        query.setEducationRequirement(safeDto.getEducationRequirement());
        query.setExpectedSalary(safeDto.getExpectedSalary());
        query.setCity(safeDto.getCity());
        Object pageData = searchPage(query, userId, clientIp);
        if (!(pageData instanceof Map<?, ?> dataMap)) {
            return pageData;
        }
        Object records = dataMap.get("records");
        return records == null ? List.of() : records;
    }

    @MethodPurpose("分页查询企业搜索历史")
    public IPage<SearchHistory> searchHistoryPage(SearchHistoryPageQueryDto query, Long userId) {
        SearchHistoryPageQueryDto safeQuery = query == null ? new SearchHistoryPageQueryDto() : query;
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        long pageNum = normalizePageNum(safeQuery.getPageNum());
        long pageSize = normalizePageSize(safeQuery.getPageSize());
        return searchHistoryMapper.selectHistoryPage(
                new Page<>(pageNum, pageSize),
                enterpriseId,
                safeQuery.getStartTime(),
                safeQuery.getEndTime()
        );
    }

    @MethodPurpose("清理企业指定时间之前的搜索历史")
    @Transactional(rollbackFor = Exception.class)
    public int cleanupSearchHistory(LocalDateTime beforeTime, Long userId) {
        if (beforeTime == null) {
            throw new BizException(400, "beforeTime不能为空");
        }
        Long enterpriseId = enterpriseIdentityService.requireEnterpriseId(userId);
        return searchHistoryMapper.deleteByEnterpriseAndBeforeTime(enterpriseId, beforeTime);
    }

    @MethodPurpose("查询公开简历详情（透传简历服务内部接口）")
    public Object getResumeDetail(Long studentId, Long userId) {
        return getResumeDetail(studentId, null, userId);
    }

    @MethodPurpose("按学生ID与简历ID查询公开简历详情（透传简历服务内部接口）")
    public Object getResumeDetail(Long studentId, Long resumeId, Long userId) {
        enterpriseIdentityService.requireEnterpriseId(userId);
        if (studentId == null) {
            throw new BizException(400, "studentId不能为空");
        }
        Result result = educationResumeTalentClient.getInternalPublicStudentDetail(
                studentId,
                resumeId,
                String.valueOf(userId),
                String.valueOf(RoleConstants.ENTERPRISE)
        );
        return requireRemoteSuccess(result, "公开简历详情查询失败");
    }

    @MethodPurpose("对比多份简历并返回详情列表")
    public List<Object> compareResume(ResumeCompareDto dto, Long userId) {
        if (dto == null || dto.getStudentIds() == null || dto.getStudentIds().isEmpty()) {
            throw new BizException(400, "studentIds不能为空");
        }
        if (dto.getStudentIds().size() > 10) {
            throw new BizException(400, "候选人对比最多支持10人");
        }
        List<Object> result = new ArrayList<>();
        for (Long studentId : dto.getStudentIds()) {
            result.add(getResumeDetail(studentId, userId));
        }
        return result;
    }

    @MethodPurpose("按筛选条件导出候选人CSV内容")
    public String exportResumeCsv(TalentSearchQueryDto query, Long userId) {
        TalentSearchQueryDto safeQuery = query == null ? new TalentSearchQueryDto() : query;
        safeQuery.setPageNum(1);
        safeQuery.setPageSize(Math.min(normalizePageSize(safeQuery.getPageSize()), MAX_EXPORT_SIZE));
        Object pageData = searchPage(safeQuery, userId, null);

        if (!(pageData instanceof Map<?, ?> dataMap)) {
            return "studentId,resumeId,resumeTitle,major,degree,skillSummary,updatedTime\n";
        }
        Object recordsObj = dataMap.get("records");
        if (!(recordsObj instanceof List<?> records)) {
            return "studentId,resumeId,resumeTitle,major,degree,skillSummary,updatedTime\n";
        }

        StringBuilder csv = new StringBuilder("studentId,resumeId,resumeTitle,major,degree,skillSummary,updatedTime\n");
        for (Object record : records) {
            Map<String, Object> map = toRecordMap(record);
            csv.append(csvValue(map.get("studentId"))).append(",")
                    .append(csvValue(map.get("resumeId"))).append(",")
                    .append(csvValue(map.get("resumeTitle"))).append(",")
                    .append(csvValue(map.get("major"))).append(",")
                    .append(csvValue(map.get("degree"))).append(",")
                    .append(csvValue(map.get("skillSummary"))).append(",")
                    .append(csvValue(map.get("updatedTime"))).append("\n");
        }
        return csv.toString();
    }

    @MethodPurpose("构造检索关键词（主关键词+技能标签）")
    private String buildKeyword(String keyword, String skillTag) {
        String keywordValue = trimToNull(keyword);
        String skillTagValue = trimToNull(skillTag);
        if (keywordValue == null) {
            return skillTagValue;
        }
        if (skillTagValue == null) {
            return keywordValue;
        }
        return keywordValue + " " + skillTagValue;
    }

    @MethodPurpose("校验远程调用成功并返回数据体")
    private Object requireRemoteSuccess(Result result, String errorPrefix) {
        if (result == null || result.getCode() == null || result.getCode() != 200) {
            throw new BizException(result == null ? 500 : result.getCode(),
                    result == null ? errorPrefix : errorPrefix + "：" + result.getMessage());
        }
        return result.getData();
    }

    @MethodPurpose("从远程分页结果中提取总数量")
    private Integer extractTotalCount(Object pageData) {
        if (!(pageData instanceof Map<?, ?> map)) {
            return 0;
        }
        Object total = map.get("total");
        if (total == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(total));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @MethodPurpose("归一化页码参数")
    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum <= 0 ? 1 : pageNum;
    }

    @MethodPurpose("归一化分页大小参数")
    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, 200);
    }

    @MethodPurpose("转为记录映射对象")
    @SuppressWarnings("unchecked")
    private Map<String, Object> toRecordMap(Object obj) {
        if (obj instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        try {
            return JSONUtil.toBean(JSONUtil.toJsonStr(obj), LinkedHashMap.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    @MethodPurpose("CSV字段值转义")
    private String csvValue(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + text + "\"";
    }

    @MethodPurpose("解析小数字段")
    private BigDecimal parseDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    @MethodPurpose("计算两点球面距离（公里）")
    private BigDecimal calculateDistanceKm(
            BigDecimal lon1,
            BigDecimal lat1,
            BigDecimal lon2,
            BigDecimal lat2
    ) {
        double earthRadiusKm = 6371.0d;
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue()))
                * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(earthRadiusKm * c).setScale(2, RoundingMode.HALF_UP);
    }

    @MethodPurpose("字符串去空并返回null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
