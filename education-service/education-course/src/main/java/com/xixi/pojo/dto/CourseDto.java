package com.xixi.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseDto {
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
     * 课程访问密码（仅创建/更新时传入，查询不回传）
     */
    private String coursePassword;

    /**
     * 密码提示
     */
    private String passwordHint;

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
