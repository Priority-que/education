package com.xixi.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教师端创建作业DTO。 */
@Data
public class HomeworkCreateDto {

    /**
     * 课程ID。 */
    private Long courseId;

    /**
     * 教师ID。 */
    private Long teacherId;

    /**
     * 作业标题。 */
    private String homeworkTitle;

    /**
     * 作业描述。 */
    private String homeworkDescription;

    /**
     * 作业类型：INDIVIDUAL/GROUP。 */
    private String homeworkType;

    /**
     * 附件地址。 */
    private String attachmentUrl;

    /**
     * 总分。 */
    private Integer totalScore;

    /**
     * 截止时间。 */
    private LocalDateTime deadline;
}