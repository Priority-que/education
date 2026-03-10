package com.xixi.pojo.dto.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 联系人创建参数。
 */
@Data
public class TalentContactCreateDto {
    private Long studentId;
    private String sourceType;
    private Long applicationId;
    private Long jobId;
    private String name;
    private String phone;
    private String email;
    private String wechat;
    private String position;
    private String status;
    private String latestStatus;
    private LocalDateTime lastContactTime;
    private String remark;
}
