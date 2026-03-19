package com.xixi.openfeign.admin.dto;

import lombok.Data;

/**
 * 内部操作日志上报参数。
 */
@Data
public class OperationLogReportRequest {
    private Long userId;
    private String userName;
    private String userRole;
    private String operationType;
    private String operationDescription;
    private String requestMethod;
    private String requestUrl;
    private String requestParams;
    private String responseResult;
    private String ipAddress;
    private String userAgent;
    private Integer executeTime;
    private Boolean status;
    private String errorMessage;
}

