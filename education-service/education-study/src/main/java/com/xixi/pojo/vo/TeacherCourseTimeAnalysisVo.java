package com.xixi.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * 教师端课程活跃时间段分析结果。
 */
@Data
public class TeacherCourseTimeAnalysisVo {

    /**
     * 课程ID。
     */
    private Long courseId;

    /**
     * 课程名称。
     */
    private String courseName;

    /**
     * 各时间段分析数据。
     */
    private List<TimeAnalysisItem> timeAnalysis;

    /**
     * 时间段分析项。
     */
    @Data
    public static class TimeAnalysisItem {
        private Integer hour;
        private String timeRange;
        private Integer studentCount;
        private Integer studyTime;
        private Integer heatValue;
    }
}
