package com.xixi.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程详情聚合VO（包含章节、视频、资料）
 */
@Data
public class CourseDetailVo {
    
    /**
     * 课程基础信息
     */
    private CourseBaseInfo courseInfo;
    
    /**
     * 章节列表（有序，每个章节包含视频和资料）
     */
    private List<ChapterDetail> chapters;
    
    /**
     * 课程基础信息
     */
    @Data
    public static class CourseBaseInfo {
        /**
         * 课程ID
         */
        private Long id;
        
        /**
         * 教师ID
         */
        private Long teacherId;
        /**
         * 教师名称
         */
        private String teacherName;
        /**
         * 课程代码
         */
        private String courseCode;
        
        /**
         * 课程名称
         */
        private String courseName;
        
        /**
         * 课程封面
         */
        private String courseCover;
        
        /**
         * 简短描述
         */
        private String shortDescription;
        
        /**
         * 详细描述
         */
        private String fullDescription;
        
        /**
         * 学分
         */
        private BigDecimal credit;
        
        /**
         * 分类ID
         */
        private Long categoryId;
        
        /**
         * 分类名称
         */
        private String categoryName;
        
        /**
         * 难度等级: BEGINNER-初级, INTERMEDIATE-中级, ADVANCED-高级
         */
        private String level;
        
        /**
         * 总学时
         */
        private Integer totalHours;
        
        /**
         * 价格
         */
        private BigDecimal price;
        
        /**
         * 状态: DRAFT-草稿, REVIEW-审核中, PUBLISHED-已发布, CLOSED-已关闭
         */
        private String status;
        
        /**
         * 是否免费: 0-收费, 1-免费
         */
        private Boolean isFree;

        /**
         * 访问类型: FREE / PAID / PASSWORD / PAID_AND_PASSWORD
         */
        private String accessType;

        /**
         * 密码提示
         */
        private String passwordHint;

        /**
         * 是否需要访问密码
         */
        private Boolean passwordRequired;
        
        /**
         * 最大学生数(0表示不限)
         */
        private Integer maxStudents;
        
        /**
         * 当前学生数
         */
        private Integer currentStudents;
        
        /**
         * 浏览数
         */
        private Integer viewCount;
        
        /**
         * 点赞数
         */
        private Integer likeCount;
        
        /**
         * 评分
         */
        private BigDecimal rating;
        
        /**
         * 评分人数
         */
        private Integer ratingCount;
        
        /**
         * 发布时间
         */
        private LocalDateTime publishedTime;
        
        /**
         * 创建时间
         */
        private LocalDateTime createdTime;
        
        /**
         * 更新时间
         */
        private LocalDateTime updatedTime;
    }
    
    /**
     * 章节详情（包含视频和资料列表）
     */
    @Data
    public static class ChapterDetail {
        /**
         * 章节ID
         */
        private Long id;
        
        /**
         * 课程ID
         */
        private Long courseId;
        
        /**
         * 章节名称
         */
        private String chapterName;
        
        /**
         * 章节描述
         */
        private String chapterDescription;
        
        /**
         * 排序
         */
        private Integer sortOrder;
        
        /**
         * 章节总时长(秒)
         */
        private Integer duration;
        
        /**
         * 创建时间
         */
        private LocalDateTime createdTime;
        
        /**
         * 更新时间
         */
        private LocalDateTime updatedTime;
        
        /**
         * 该章节的视频列表（有序）
         */
        private List<VideoInfo> videos;
        
        /**
         * 该章节的资料列表（有序）
         */
        private List<MaterialInfo> materials;
    }
    
    /**
     * 视频信息
     */
    @Data
    public static class VideoInfo {
        /**
         * 视频ID
         */
        private Long id;
        
        /**
         * 章节ID
         */
        private Long chapterId;
        
        /**
         * 视频名称
         */
        private String videoName;
        
        /**
         * 视频地址
         */
        private String videoUrl;
        
        /**
         * 封面图
         */
        private String coverImage;
        
        /**
         * 视频时长(秒)
         */
        private Integer duration;
        
        /**
         * 文件大小(字节)
         */
        private Long fileSize;
        
        /**
         * 视频格式
         */
        private String videoFormat;
        
        /**
         * 分辨率
         */
        private String resolution;
        
        /**
         * 排序
         */
        private Integer sortOrder;
        
        /**
         * 状态: 0-禁用, 1-启用
         */
        private Boolean status;
        
        /**
         * 播放次数
         */
        private Integer viewCount;
        
        /**
         * 创建时间
         */
        private LocalDateTime createdTime;
        
        /**
         * 更新时间
         */
        private LocalDateTime updatedTime;
    }
    
    /**
     * 资料信息
     */
    @Data
    public static class MaterialInfo {
        /**
         * 资料ID
         */
        private Long id;
        
        /**
         * 课程ID
         */
        private Long courseId;
        
        /**
         * 章节ID
         */
        private Long chapterId;
        
        /**
         * 资料名称
         */
        private String materialName;
        
        /**
         * 资料类型: PDF, PPT, DOC, EXCEL, ZIP
         */
        private String materialType;
        
        /**
         * 文件地址
         */
        private String fileUrl;
        
        /**
         * 文件大小
         */
        private Long fileSize;
        
        /**
         * 下载次数
         */
        private Integer downloadCount;
        
        /**
         * 资料描述
         */
        private String description;
        
        /**
         * 排序
         */
        private Integer sortOrder;
        
        /**
         * 创建时间
         */
        private LocalDateTime createdTime;
    }
}


















