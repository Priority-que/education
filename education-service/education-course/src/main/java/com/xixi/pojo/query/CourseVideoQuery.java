package com.xixi.pojo.query;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程视频查询条件
 */
@Data
public class CourseVideoQuery {

    /** 页码 */
    private Integer pageNum = 1;

    /** 页大小 */
    private Integer pageSize = 10;

    /** 章节ID */
    private Long chapterId;

    /** 视频名称（模糊） */
    private String videoName;

    /** 视频格式 */
    private String videoFormat;

    /** 状态: 0-禁用, 1-启用 */
    private Boolean status;

    /** 开始时间（按创建时间） */
    private LocalDateTime beginTime;

    /** 结束时间（按创建时间） */
    private LocalDateTime endTime;
}
