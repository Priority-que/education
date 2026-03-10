package com.xixi.mq;

/**
 * 课程模块消息队列常量。
 */
public final class CourseMqConstant {

    /**
     * 课程事件交换机。
     */
    public static final String COURSE_EVENT_EXCHANGE = "education.course.event.exchange";

    /**
     * 课程学生人数变更队列。
     */
    public static final String COURSE_STUDENT_COUNT_QUEUE = "education.course.student.count.queue";

    /**
     * 课程学生人数变更路由键。
     */
    public static final String COURSE_STUDENT_COUNT_ROUTING_KEY = "course.student.count.change";

    private CourseMqConstant() {
    }
}
