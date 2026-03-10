package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("teachers")
public class Teachers {
    
    /**
     * 教师ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 教师工号
     */
    private String teacherNumber;
    
    /**
     * 职称
     */
    private String title;
    
    /**
     * 部门
     */
    private String department;
    
    /**
     * 研究方向
     */
    private String researchArea;
    
    /**
     * 个人简介
     */
    private String introduction;
    
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

