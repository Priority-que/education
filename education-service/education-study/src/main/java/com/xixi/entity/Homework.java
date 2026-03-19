package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("homework")
public class Homework {
    
    /**
     * 作业ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 教师ID
     */
    private Long teacherId;
    
    /**
     * 作业标题
     */
    private String homeworkTitle;
    
    /**
     * 作业描述
     */
    private String homeworkDescription;
    
    /**
     * 作业类型: INDIVIDUAL-个人作业, GROUP-小组作业
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
     * 状态: DRAFT-草稿, PUBLISHED-已发布, CLOSED-已截止
     */
    private String status;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}

