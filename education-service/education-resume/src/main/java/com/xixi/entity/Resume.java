package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("resume")
public class Resume {
    
    /**
     * 简历ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 简历标题
     */
    private String resumeTitle;
    
    /**
     * 简历模板
     */
    private String resumeTemplate;
    
    /**
     * 简历头像
     */
    private String avatarUrl;
    
    /**
     * 联系邮箱
     */
    private String contactEmail;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 微信
     */
    private String wechat;
    
    /**
     * 领英链接
     */
    private String linkedin;
    
    /**
     * GitHub链接
     */
    private String github;
    
    /**
     * 自我介绍
     */
    private String selfIntroduction;
    
    /**
     * 职业目标
     */
    private String careerObjective;
    
    /**
     * 可见性: PRIVATE-私有, PUBLIC-公开, SELECTED-选择可见
     */
    private String visibility;
    
    /**
     * 查看次数
     */
    private Integer viewCount;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 是否默认简历: 0-否, 1-是
     */
    private Boolean isDefault;
    
    /**
     * 状态: 0-禁用, 1-启用
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

