package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("talent_favorite")
public class TalentFavorite {
    
    /**
     * 收藏ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 企业ID
     */
    private Long enterpriseId;
    
    /**
     * 简历ID
     */
    private Long resumeId;
    
    /**
     * 学生ID(冗余)
     */
    private Long studentId;
    
    /**
     * 标签(JSON数组)
     */
    private String tags;
    
    /**
     * 评分(1-5星)
     */
    private Integer rating;
    
    /**
     * 备注
     */
    private String notes;
    
    /**
     * 状态: INTERESTED-感兴趣, CONTACTED-已联系, INTERVIEWED-已面试, OFFERED-已发Offer, HIRED-已录用, REJECTED-已拒绝
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

