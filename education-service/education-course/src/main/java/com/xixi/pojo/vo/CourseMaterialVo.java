package com.xixi.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程资料 VO
 */
@Data
public class CourseMaterialVo {

    /** 资料ID */
    private Long id;

    /** 课程ID */
    private Long courseId;

    /** 章节ID */
    private Long chapterId;

    /** 资料名称 */
    private String materialName;

    /** 资料类型: PDF, PPT, DOC, EXCEL, ZIP */
    private String materialType;

    /** 文件地址 */
    private String fileUrl;

    /** 文件大小 */
    private Long fileSize;

    /** 下载次数 */
    private Integer downloadCount;

    /** 资料描述 */
    private String description;

    /** 排序 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdTime;
}
