package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import com.xixi.constant.RoleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 公开简历访问事件生产者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResumePublicAccessEventProducer {
    private static final String EVENT_DETAIL_VIEWED = "RESUME_PUBLIC_DETAIL_VIEWED";
    private static final String EVENT_PAGE_SEARCHED = "RESUME_PUBLIC_PAGE_SEARCHED";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布公开简历详情访问事件。
     */
    @MethodPurpose("发布公开简历详情访问事件")
    public void publishDetailViewed(Long resumeId, Long viewerId, Integer viewerRole) {
        ResumePublicAccessEvent event = new ResumePublicAccessEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(EVENT_DETAIL_VIEWED);
        event.setResumeId(resumeId);
        event.setViewerId(viewerId);
        event.setViewerType(toViewerType(viewerRole));
        event.setEventTime(LocalDateTime.now());
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * 发布公开简历分页检索事件。
     */
    @MethodPurpose("发布公开简历分页检索事件")
    public void publishPageSearched(Long viewerId, String keyword, String major, String degree, Integer pageNum, Integer pageSize) {
        ResumePublicAccessEvent event = new ResumePublicAccessEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(EVENT_PAGE_SEARCHED);
        event.setViewerId(viewerId);
        event.setViewerType("ENTERPRISE");
        event.setKeyword(keyword);
        event.setMajor(major);
        event.setDegree(degree);
        event.setPageNum(pageNum);
        event.setPageSize(pageSize);
        event.setEventTime(LocalDateTime.now());
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * 事务提交后（或无事务时）投递公开访问事件。
     */
    @MethodPurpose("事务提交后（或无事务时）发送公开访问事件到 RabbitMQ")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void sendAfterCommit(ResumePublicAccessEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    ResumeMqConstant.RESUME_EVENT_EXCHANGE,
                    ResumeMqConstant.RESUME_PUBLIC_ACCESS_ROUTING_KEY,
                    JSONUtil.toJsonStr(event)
            );
            log.info("resume public access event published, eventId={}, type={}, resumeId={}, viewerId={}",
                    event.getEventId(), event.getEventType(), event.getResumeId(), event.getViewerId());
        } catch (Exception e) {
            log.error("resume public access event publish failed, eventId={}, type={}",
                    event.getEventId(), event.getEventType(), e);
        }
    }

    @MethodPurpose("根据角色码映射访问者类型")
    private String toViewerType(Integer viewerRole) {
        if (viewerRole == null) {
            return "VISITOR";
        }
        if (viewerRole == RoleConstants.ADMIN) {
            return "ADMIN";
        }
        if (viewerRole == RoleConstants.STUDENT) {
            return "STUDENT";
        }
        if (viewerRole == RoleConstants.ENTERPRISE) {
            return "ENTERPRISE";
        }
        return "VISITOR";
    }
}
