package com.xixi.service;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.EnterpriseTalentStatistics;
import com.xixi.mapper.EnterpriseTalentStatisticsMapper;
import com.xixi.mq.TalentDomainEvent;
import com.xixi.mq.TalentDomainEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * 人才统计异步更新服务。
 */
@Service
@RequiredArgsConstructor
public class TalentStatisticsAsyncService {
    private final EnterpriseTalentStatisticsMapper enterpriseTalentStatisticsMapper;

    @MethodPurpose("消费人才域事件并按事件类型更新当日统计快照")
    @Transactional(rollbackFor = Exception.class)
    public void consumeDomainEvent(TalentDomainEvent event) {
        if (event == null || event.getEnterpriseId() == null || !StringUtils.hasText(event.getEventType())) {
            return;
        }
        Long enterpriseId = event.getEnterpriseId();
        LocalDate statDate = LocalDate.now();
        ensureDailySnapshot(enterpriseId, statDate);

        int totalFavoritesDelta = 0;
        int newFavoritesDelta = 0;
        int totalContactsDelta = 0;
        int newContactsDelta = 0;
        int totalInterviewsDelta = 0;
        int newInterviewsDelta = 0;
        int totalHiresDelta = 0;
        int newHiresDelta = 0;
        int totalSearchesDelta = 0;

        String eventType = event.getEventType();
        if (TalentDomainEventProducer.EVENT_SEARCH_EXECUTED.equals(eventType)) {
            totalSearchesDelta = 1;
        } else if (TalentDomainEventProducer.EVENT_FAVORITE_CREATED.equals(eventType)) {
            totalFavoritesDelta = 1;
            newFavoritesDelta = 1;
        } else if (TalentDomainEventProducer.EVENT_FAVORITE_DELETED.equals(eventType)) {
            totalFavoritesDelta = -1;
        } else if (TalentDomainEventProducer.EVENT_FAVORITE_STATUS_CHANGED.equals(eventType)) {
            String newStatus = extractPayloadField(event.getPayload(), "newStatus");
            if ("CONTACTED".equals(newStatus)) {
                totalContactsDelta = 1;
                newContactsDelta = 1;
            } else if ("INTERVIEWED".equals(newStatus)) {
                totalInterviewsDelta = 1;
                newInterviewsDelta = 1;
            } else if ("HIRED".equals(newStatus)) {
                totalHiresDelta = 1;
                newHiresDelta = 1;
            }
        } else if (TalentDomainEventProducer.EVENT_COMMUNICATION_SENT.equals(eventType)) {
            totalContactsDelta = 1;
            newContactsDelta = 1;
        }

        if (totalFavoritesDelta == 0
                && newFavoritesDelta == 0
                && totalContactsDelta == 0
                && newContactsDelta == 0
                && totalInterviewsDelta == 0
                && newInterviewsDelta == 0
                && totalHiresDelta == 0
                && newHiresDelta == 0
                && totalSearchesDelta == 0) {
            return;
        }

        enterpriseTalentStatisticsMapper.increaseDailyMetric(
                enterpriseId,
                statDate,
                totalFavoritesDelta,
                newFavoritesDelta,
                totalContactsDelta,
                newContactsDelta,
                totalInterviewsDelta,
                newInterviewsDelta,
                totalHiresDelta,
                newHiresDelta,
                totalSearchesDelta
        );
    }

    @MethodPurpose("确保企业当日统计快照存在，不存在则初始化零值快照")
    private void ensureDailySnapshot(Long enterpriseId, LocalDate statDate) {
        EnterpriseTalentStatistics current = enterpriseTalentStatisticsMapper.selectByEnterpriseAndDate(enterpriseId, statDate);
        if (current != null) {
            return;
        }
        EnterpriseTalentStatistics init = new EnterpriseTalentStatistics();
        init.setEnterpriseId(enterpriseId);
        init.setStatDate(statDate);
        init.setTotalFavorites(0);
        init.setNewFavorites(0);
        init.setTotalContacts(0);
        init.setNewContacts(0);
        init.setTotalInterviews(0);
        init.setNewInterviews(0);
        init.setTotalHires(0);
        init.setNewHires(0);
        init.setTotalSearches(0);
        enterpriseTalentStatisticsMapper.insert(init);
    }

    @MethodPurpose("从事件载荷中提取字符串字段")
    private String extractPayloadField(String payload, String key) {
        if (!StringUtils.hasText(payload)) {
            return null;
        }
        try {
            Object value = JSONUtil.parseObj(payload).get(key);
            return value == null ? null : String.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }
}
