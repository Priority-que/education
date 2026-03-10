package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业VO
 */
@Data
public class HomeworkVo {
    
    /**
     * 作业ID
     */
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 教师ID
     */
    private Long teacherId;
    
    /**
     * 教师姓名
     */
    private String teacherName;
    
    /**
     * 作业标题
     */
    private String homeworkTitle;
    
    /**
     * 作业描述
     */
    private String homeworkDescription;
    
    /**
     * 作业类型
     */
    private String homeworkType;
    
    /**
     * 附件地址
     */
    private String attachmentUrl;
    
    /**
     * 总分
     */
    private Integer totalScore;
    
    /**
     * 截止时间
     */
    private LocalDateTime deadline;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 提交状态（学生是否已提交）
     */
    private String submissionStatus;
    
    /**
     * 得分
     */
    private BigDecimal score;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
