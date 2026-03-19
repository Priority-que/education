package com.xixi.pojo.dto.admin;

import lombok.Data;

/**
 * 操作日志上报请求参数。
 */
@Data
public class OperationLogReportDto {
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
