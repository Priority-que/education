package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("search_history")
public class SearchHistory {
    
    /**
     * 搜索记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 企业ID
     */
    private Long enterpriseId;
    
    /**
     * 搜索关键词
     */
    private String searchKeyword;
    
    /**
     * 搜索筛选条件
     */
    private String searchFilters;
    
    /**
     * 结果数量
     */
    private Integer resultCount;
    
    /**
     * 搜索时间
     */
    private LocalDateTime searchTime;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}

