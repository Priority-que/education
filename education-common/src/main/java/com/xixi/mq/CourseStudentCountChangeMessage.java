package com.xixi.mq;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程学生人数变更消息。
 */
@Data
public class CourseStudentCountChangeMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一ID，用于幂等处理。
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
     * 事件发生时间。
     */
    private LocalDateTime eventTime;
}
