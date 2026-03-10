package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业端联系人分页项视图对象。
 */
@Data
public class TalentContactPageVo {
    private Long contactId;
    private Long enterpriseId;
    private Long studentId;
    private String sourceType;
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private String name;
    private String phone;
    private String email;
    private String wechat;
    private String position;
    private String status;
    private String latestStatus;
    private LocalDateTime latestCommunicationTime;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
