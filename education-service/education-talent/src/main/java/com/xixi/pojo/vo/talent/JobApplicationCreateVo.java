package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建投递返回视图对象。
 */
@Data
public class JobApplicationCreateVo {
    private Long applicationId;
    private String applicationNo;
    private String status;
    private LocalDateTime submittedTime;
}
