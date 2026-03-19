package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course_comment")
public class CourseComment {
    
    /**
     * 评价ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 视频ID（可选，未传表示课程维度评论）
     */
    private Long videoId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 评分
     */
    private BigDecimal rating;
    
    /**
     * 评价内容
     */
    private String commentContent;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 回复数
     */

    private Integer replyCount;
    
    /**
     * 是否匿名: 0-否, 1-是
     */
    private Boolean isAnonymous;
    
    /**
     * 状态: 0-隐藏, 1-显示
     */
    private Boolean status;
    
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
