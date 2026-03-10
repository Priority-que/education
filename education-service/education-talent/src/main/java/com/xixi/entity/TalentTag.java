package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("talent_tag")
public class TalentTag {
    
    /**
     * 标签ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 企业ID
     */
    private Long enterpriseId;
    
    /**
     * 标签名称
     */
    private String tagName;
    
    /**
     * 标签颜色
     */
    private String tagColor;
    
    /**
     * 标签描述
     */
    private String description;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}

