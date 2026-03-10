package com.xixi.pojo.dto.message;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内部投递：按角色发送请求参数。
 */
@Data
public class InternalSendToRoleDto {
    /**
     * ADMIN/STUDENT/TEACHER/ENTERPRISE 或 1/2/3/4。
     */
    private List<Object> roleCodes;
    /**
     * SYNC/MQ，默认SYNC。
     */
    private String deliverMode;
    /**
     * SYSTEM/COURSE/CERTIFICATE/JOB/OTHER
     */
    private String messageType;
    private String messageTitle;
    private String messageContent;
    private Long relatedId;
    private String relatedType;
    /**
     * 0-普通，1-重要，2-紧急
     */
    private Integer priority;
    private LocalDateTime expiryTime;
}

