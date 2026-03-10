package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("job_posting")
public class JobPosting {
    
    /**
     * 职位ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 企业ID
     */
    private Long enterpriseId;
    
    /**
     * 职位名称
     */
    private String jobTitle;
    
    /**
     * 职位类型: FULL_TIME-全职, PART_TIME-兼职, INTERNSHIP-实习
     */
    private String jobType;
    
    /**
     * 职位类别
     */
    private String jobCategory;
    
    /**
     * 工作地点
     */
    private String workLocation;
    
    /**
     * 薪资范围
     */
    private String salaryRange;
    
    /**
     * 经验要求
     */
    private String experienceRequirement;
    
    /**
     * 学历要求
     */
    private String educationRequirement;
    
    /**
     * 职位描述
     */
    private String jobDescription;
    
    /**
     * 任职要求
     */
    private String requirements;
    
    /**
     * 福利待遇
     */
    private String benefits;
    
    /**
     * 招聘人数
     */
    private Integer recruitmentNumber;
    
    /**
     * 申请截止日期
     */
    private LocalDate applicationDeadline;
    
    /**
     * 联系邮箱
     */
    private String contactEmail;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 状态: DRAFT-草稿, PUBLISHED-已发布, CLOSED-已关闭, EXPIRED-已过期
     */
    private String status;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 浏览数
     */
    private Integer viewCount;
    
    /**
     * 申请数
     */
    private Integer applyCount;
    
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

