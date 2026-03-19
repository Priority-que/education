package com.xixi.pojo.vo;

import lombok.Data;
import java.util.List;

/**
 * 学习时长统计VO
 */
@Data
public class LearningStatisticsTimeVo {
    
    /**
     * 时间序列数据
     */
    private List<TimeData> timeDataList;
    
    @Data
    public static class TimeData {
        /**
         * 日期
         */
        private String date;
        
        /**
         * 学习时长（小时）
         */
        private Double studyHours;
    }
}
