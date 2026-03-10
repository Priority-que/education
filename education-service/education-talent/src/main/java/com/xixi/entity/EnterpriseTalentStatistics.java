package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("enterprise_talent_statistics")
public class EnterpriseTalentStatistics {
    
    /**
     * 统计ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 企业ID
     */
    private Long enterpriseId;
    
    /**
     * 统计日期
     */
    private LocalDate statDate;
    
    /**
     * 总收藏数
     */
    private Integer totalFavorites;
    
    /**
     * 新增收藏数
     */
    private Integer newFavorites;
    
    /**
     * 总联系数
     */
    private Integer totalContacts;
    
    /**
     * 新增联系数
     */
    private Integer newContacts;
    
    /**
     * 总面试数
     */
    private Integer totalInterviews;
    
    /**
     * 新增面试数
     */
    private Integer newInterviews;
    
    /**
     * 总录用数
     */
    private Integer totalHires;
    
    /**
     * 新增录用数
     */
    private Integer newHires;
    
    /**
     * 总搜索数
     */
    private Integer totalSearches;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}

