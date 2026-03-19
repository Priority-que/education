package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {
    
    /**
     * 课程ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 教师ID
     */
    private Long teacherId;
    
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
