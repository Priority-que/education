package com.xixi.pojo.query;

import lombok.Data;

/**
 * 教师端学生学习监控查询条件。
 */
@Data
public class TeacherLearningMonitorQuery {

    /**
     * 课程ID（必填）。
     */
    private Long courseId;

    /**
     * 学生ID（可选）。
     */
    private Long studentId;

    /**
     * 章节ID（可选）。
     */
    private Long chapterId;
}

