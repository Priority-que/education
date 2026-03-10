package com.xixi.mq;

import cn.hutool.json.JSONUtil;
import com.xixi.entity.CourseMqConsumeLog;
import com.xixi.mapper.CourseMapper;
import com.xixi.mapper.CourseMqConsumeLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 课程学生人数变更消息消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseStudentCountMessageConsumer {
    private static final String TX_MODE_MQ = "mq";

    private final CourseMapper courseMapper;
    private final CourseMqConsumeLogMapper courseMqConsumeLogMapper;
    @Value("${education.tx.mode:seata}")
    private String txMode;

    /**
     * 消费课程学生人数变更消息，并执行人数增减与幂等处理。
     */
    @Transactional(rollbackFor = Exception.class)
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = CourseMqConstant.COURSE_STUDENT_COUNT_QUEUE, durable = "true"),
            exchange = @Exchange(value = CourseMqConstant.COURSE_EVENT_EXCHANGE, type = ExchangeTypes.TOPIC, durable = "true"),
            key = CourseMqConstant.COURSE_STUDENT_COUNT_ROUTING_KEY
    ))
    public void consume(String messageBody) {
        if (!TX_MODE_MQ.equalsIgnoreCase(txMode)) {
            log.info("当前事务模式为{}，忽略课程人数MQ消息", txMode);
            return;
        }

        CourseStudentCountChangeMessage message;
        try {
            message = JSONUtil.toBean(messageBody, CourseStudentCountChangeMessage.class);
        } catch (Exception e) {
            log.error("解析课程人数变更消息失败,body={}", messageBody, e);
            return;
        }

        if (message == null || message.getCourseId() == null || message.getEventId() == null) {
            log.warn("课程人数变更消息参数不完整,body={}", messageBody);
            return;
        }

        Integer consumedCount = courseMqConsumeLogMapper.countByEventId(message.getEventId());
        if (consumedCount != null && consumedCount > 0) {
            log.info("重复消息已忽略,eventId={}", message.getEventId());
            return;
        }

        try {
            CourseMqConsumeLog consumeLog = new CourseMqConsumeLog();
            consumeLog.setEventId(message.getEventId());
            consumeLog.setCourseId(message.getCourseId());
            consumeLog.setStudentId(message.getStudentId());
            consumeLog.setStatus(message.getStatus());
            consumeLog.setConsumedTime(LocalDateTime.now());
            courseMqConsumeLogMapper.insert(consumeLog);
        } catch (DuplicateKeyException duplicateKeyException) {
            log.info("重复消息已忽略,eventId={}", message.getEventId());
            return;
        }

        if (courseMapper.selectById(message.getCourseId()) == null) {
            log.warn("课程不存在，忽略人数变更,eventId={},courseId={}",
                    message.getEventId(), message.getCourseId());
            return;
        }

        int affectedRows;
        if (message.getStatus() != null && message.getStatus() == 1) {
            affectedRows = courseMapper.addStudentNumber(message.getCourseId());
        } else {
            affectedRows = courseMapper.reduceStudentNumber(message.getCourseId());
        }

        if (affectedRows <= 0) {
            log.warn("课程人数变更未生效,eventId={},courseId={},status={}",
                    message.getEventId(), message.getCourseId(), message.getStatus());
            return;
        }

        log.info("消费课程人数变更消息成功,eventId={},courseId={},status={}",
                message.getEventId(), message.getCourseId(), message.getStatus());
    }
}
