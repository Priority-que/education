package com.xixi.service;

import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.EnterpriseTalentStatistics;
import com.xixi.exception.BizException;
import com.xixi.mapper.CommunicationRecordMapper;
import com.xixi.mapper.EnterpriseTalentStatisticsMapper;
import com.xixi.mapper.JobPostingMapper;
import com.xixi.mapper.SearchHistoryMapper;
import com.xixi.mapper.TalentFavoriteMapper;
import com.xixi.mq.TalentDomainEventProducer;
import com.xixi.pojo.dto.talent.FavoriteStatusSyncDto;
import com.xixi.pojo.dto.talent.TalentStatDailyReportDto;
import com.xixi.pojo.vo.talent.EnterpriseTalentSummaryVo;
import com.xixi.pojo.vo.talent.TalentStatDailyResultVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 人才服务内部协同接口服务。
 */
@Service
@RequiredArgsConstructor
public class TalentInternalService {
    private static final String STATUS_INTERVIEWED = "INTERVIEWED";
    private static final String STATUS_OFFERED = "OFFERED";
    private static final String STATUS_HIRED = "HIRED";

    private final TalentFavoriteMapper talentFavoriteMapper;
    private final CommunicationRecordMapper communicationRecordMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final JobPostingMapper jobPostingMapper;
    private final EnterpriseTalentStatisticsMapper enterpriseTalentStatisticsMapper;
    private final TalentFavoriteService talentFavoriteService;
    private final TalentDomainEventProducer talentDomainEventProducer;

    @MethodPurpose("内部接口：重建企业某日人才运营统计快照")
    @Transactional(rollbackFor = Exception.class)
    public TalentStatDailyResultVo rebuildDailyStat(TalentStatDailyReportDto dto) {
        TalentStatDailyReportDto safeDto = dto == null ? new TalentStatDailyReportDto() : dto;
        LocalDate statDate = safeDto.getStatDate() == null ? LocalDate.now() : safeDto.getStatDate();
        Set<Long> enterpriseIds = new HashSet<>();
        if (safeDto.getEnterpriseId() != null) {
            enterpriseIds.add(safeDto.getEnterpriseId());
        } else {
            enterpriseIds.addAll(collectEnterpriseIds());
        }
        int successCount = 0;
        for (Long enterpriseId : enterpriseIds) {
            if (enterpriseId == null) {
                continue;
            }
            buildAndSaveSnapshot(enterpriseId, statDate);
            successCount++;
        }
        TalentStatDailyResultVo vo = new TalentStatDailyResultVo();
        vo.setStatDate(statDate);
        vo.setEnterpriseCount(enterpriseIds.size());
        vo.setSuccessCount(successCount);
        return vo;
    }

    @MethodPurpose("内部接口：查询企业人才运营摘要")
    public EnterpriseTalentSummaryVo getEnterpriseSummary(Long enterpriseId) {
        if (enterpriseId == null) {
            throw new BizException(400, "enterpriseId不能为空");
        }
        EnterpriseTalentStatistics latest = enterpriseTalentStatisticsMapper.selectLatestByEnterpriseId(enterpriseId);
        EnterpriseTalentSummaryVo vo = new EnterpriseTalentSummaryVo();
        vo.setEnterpriseId(enterpriseId);
        if (latest != null) {
            vo.setTotalFavorites(nullToZero(latest.getTotalFavorites()));
            vo.setTotalContacts(nullToZero(latest.getTotalContacts()));
            vo.setTotalInterviews(nullToZero(latest.getTotalInterviews()));
            vo.setTotalHires(nullToZero(latest.getTotalHires()));
            vo.setTotalSearches(nullToZero(latest.getTotalSearches()));
            vo.setLastStatDate(latest.getStatDate());
            return vo;
        }
        vo.setTotalFavorites(nullToZero(talentFavoriteMapper.countTotalByEnterprise(enterpriseId)));
        vo.setTotalContacts(nullToZero(communicationRecordMapper.countTotalByEnterprise(enterpriseId)));
        vo.setTotalInterviews(
                nullToZero(talentFavoriteMapper.countByEnterpriseAndStatus(enterpriseId, STATUS_INTERVIEWED))
                        + nullToZero(talentFavoriteMapper.countByEnterpriseAndStatus(enterpriseId, STATUS_OFFERED))
                        + nullToZero(talentFavoriteMapper.countByEnterpriseAndStatus(enterpriseId, STATUS_HIRED))
        );
        vo.setTotalHires(nullToZero(talentFavoriteMapper.countByEnterpriseAndStatus(enterpriseId, STATUS_HIRED)));
        vo.setTotalSearches(nullToZero(searchHistoryMapper.countTotalByEnterprise(enterpriseId)));
        vo.setLastStatDate(null);
        return vo;
    }

    @MethodPurpose("内部接口：同步企业收藏状态变更")
    @Transactional(rollbackFor = Exception.class)
    public void syncFavoriteStatus(FavoriteStatusSyncDto dto) {
        if (dto == null || dto.getEnterpriseId() == null || dto.getFavoriteId() == null) {
            throw new BizException(400, "enterpriseId和favoriteId不能为空");
        }
        talentFavoriteService.syncFavoriteStatus(dto.getEnterpriseId(), dto.getFavoriteId(), dto.getStatus());
    }

    @MethodPurpose("构建并保存企业单日统计快照")
    private void buildAndSaveSnapshot(Long enterpriseId, LocalDate statDate) {
        int totalFavorites = nullToZero(talentFavoriteMapper.countTotalByEnterprise(enterpriseId));
        int newFavorites = nullToZero(talentFavoriteMapper.countCreatedByEnterpriseAndDate(enterpriseId, statDate));
        int totalContacts = nullToZero(communicationRecordMapper.countTotalByEnterprise(enterpriseId));
        int newContacts = nullToZero(communicationRecordMapper.countCreatedByEnterpriseAndDate(enterpriseId, statDate));
        int totalInterviews = nullToZero(talentFavoriteMapper.countByEnterpriseAndStatus(enterpriseId, STATUS_INTERVIEWED))
                + nullToZero(talentFavoriteMapper.countByEnterpriseAndStatus(enterpriseId, STATUS_OFFERED))
                + nullToZero(talentFavoriteMapper.countByEnterpriseAndStatus(enterpriseId, STATUS_HIRED));
        int newInterviews = nullToZero(talentFavoriteMapper.countStatusUpdatedByEnterpriseAndDate(enterpriseId, STATUS_INTERVIEWED, statDate));
        int totalHires = nullToZero(talentFavoriteMapper.countByEnterpriseAndStatus(enterpriseId, STATUS_HIRED));
        int newHires = nullToZero(talentFavoriteMapper.countStatusUpdatedByEnterpriseAndDate(enterpriseId, STATUS_HIRED, statDate));
        int totalSearches = nullToZero(searchHistoryMapper.countTotalByEnterprise(enterpriseId));

        EnterpriseTalentStatistics existing = enterpriseTalentStatisticsMapper.selectByEnterpriseAndDate(enterpriseId, statDate);
        if (existing == null) {
            EnterpriseTalentStatistics stat = new EnterpriseTalentStatistics();
            stat.setEnterpriseId(enterpriseId);
            stat.setStatDate(statDate);
            stat.setTotalFavorites(totalFavorites);
            stat.setNewFavorites(newFavorites);
            stat.setTotalContacts(totalContacts);
            stat.setNewContacts(newContacts);
            stat.setTotalInterviews(totalInterviews);
            stat.setNewInterviews(newInterviews);
            stat.setTotalHires(totalHires);
            stat.setNewHires(newHires);
            stat.setTotalSearches(totalSearches);
            enterpriseTalentStatisticsMapper.insert(stat);
        } else {
            existing.setTotalFavorites(totalFavorites);
            existing.setNewFavorites(newFavorites);
            existing.setTotalContacts(totalContacts);
            existing.setNewContacts(newContacts);
            existing.setTotalInterviews(totalInterviews);
            existing.setNewInterviews(newInterviews);
            existing.setTotalHires(totalHires);
            existing.setNewHires(newHires);
            existing.setTotalSearches(totalSearches);
            enterpriseTalentStatisticsMapper.updateById(existing);
        }
        talentDomainEventProducer.publish(
                TalentDomainEventProducer.EVENT_STAT_REBUILD,
                enterpriseId,
                null,
                java.util.Map.of("statDate", statDate.toString())
        );
    }

    @MethodPurpose("收集人才域活跃企业ID集合")
    private Set<Long> collectEnterpriseIds() {
        Set<Long> ids = new HashSet<>();
        addAll(ids, talentFavoriteMapper.selectDistinctEnterpriseIds());
        addAll(ids, communicationRecordMapper.selectDistinctEnterpriseIds());
        addAll(ids, searchHistoryMapper.selectDistinctEnterpriseIds());
        addAll(ids, jobPostingMapper.selectDistinctEnterpriseIds());
        addAll(ids, enterpriseTalentStatisticsMapper.selectDistinctEnterpriseIds());
        return ids;
    }

    @MethodPurpose("向集合中批量加入非空ID")
    private void addAll(Set<Long> ids, List<Long> values) {
        if (values == null) {
            return;
        }
        for (Long value : values) {
            if (value != null) {
                ids.add(value);
            }
        }
    }

    @MethodPurpose("空值转0")
    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
