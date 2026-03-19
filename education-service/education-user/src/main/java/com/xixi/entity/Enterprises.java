package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("enterprises")
public class Enterprises {
    
    /**
     * 企业ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 公司名称
     */
    private String companyName;
    
    /**
     * 行业
     */
    private String industry;
    
    /**
     * 公司规模
     */
    private String companySize;
    
    /**
     * 公司地址
     */
    private String companyAddress;
    
    /**
     * 联系人
     */
    private String contactPerson;
    
    /**
     * 公司简介
     */
    private String companyIntroduction;
    
    /**
     * 认证状态: 0-未认证, 1-认证中, 2-已认证
     */
    private Integer verificationStatus;
    
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

