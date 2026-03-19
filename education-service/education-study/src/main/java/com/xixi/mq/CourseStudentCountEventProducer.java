package com.xixi.mq;

import cn.hutool.json.JSONUtil;
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
 * 课程学生人数变更消息生产者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseStudentCountEventProducer {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布课程学生人数变更事件（事务提交后发送MQ）。
     */
    public void publish(Long courseId, Long studentId, Integer status) {
        CourseStudentCountChangeMessage message = new CourseStudentCountChangeMessage();
        message.setEventId(UUID.randomUUID().toString().replace("-", ""));
        message.setCourseId(courseId);
        message.setStudentId(studentId);
        message.setStatus(status);
        message.setEventTime(LocalDateTime.now());
        applicationEventPublisher.publishEvent(message);
    }

    /**
     * 事务提交后发送课程学生人数变更消息。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterCommit(CourseStudentCountChangeMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    CourseMqConstant.COURSE_EVENT_EXCHANGE,
                    CourseMqConstant.COURSE_STUDENT_COUNT_ROUTING_KEY,
                    JSONUtil.toJsonStr(message));
            log.info("发送课程人数变更消息成功,eventId={},courseId={},status={}",
                    message.getEventId(), message.getCourseId(), message.getStatus());
        } catch (Exception e) {
            log.error("发送课程人数变更消息失败,eventId={},courseId={},status={},error={}",
                    message.getEventId(), message.getCourseId(), message.getStatus(), e.getMessage(), e);
        }
    }
}
