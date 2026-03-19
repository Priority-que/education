package com.xixi.pojo.query;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程资料查询条件
 */
@Data
public class CourseMaterialQuery {

    /** 页码 */
    private Integer pageNum = 1;

    /** 页大小 */
    private Integer pageSize = 10;

    /** 课程ID */
    private Long courseId;

    /** 章节ID */
    private Long chapterId;

    /** 资料名称（模糊） */
    private String materialName;

    /** 资料类型: PDF, PPT, DOC, EXCEL, ZIP */
    private String materialType;

    /** 开始时间（按创建时间） */
    private LocalDateTime beginTime;

    /** 结束时间（按创建时间） */
    private LocalDateTime endTime;
}
