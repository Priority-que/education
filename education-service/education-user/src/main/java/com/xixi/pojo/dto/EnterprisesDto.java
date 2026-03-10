package com.xixi.pojo.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnterprisesDto {
    /**
     * 企业ID
     */
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
    private LocalDateTime createdTime;
    /**
     * 更新时间
     */

    private LocalDateTime updatedTime;
}
