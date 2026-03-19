package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程消息消费日志（用于幂等控制）。
 */
@Data
@TableName("course_mq_consume_log")
public class CourseMqConsumeLog {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息唯一ID。
     */
    private String eventId;

    /**
     * 课程ID。
     */
    private Long courseId;

    /**
     * 学生ID。
     */
    private Long studentId;

    /**
     * 变更类型：1-加一，0-减一。
     */
    private Integer status;

    /**
     * 消费时间。
     */
    private LocalDateTime consumedTime;
}
