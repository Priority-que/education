package com.xixi.pojo.vo.message;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeacherSystemMessageHistoryVo {
    private Long id;
    private String messageType;
    private String messageTitle;
    private String messageContent;
    private Integer priority;
    private String targetType;
    private String targetSpecJson;
    private String status;
    private Boolean canPublish;
    private LocalDateTime publishTime;
    private LocalDateTime expiryTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
