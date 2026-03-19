package com.xixi.service.support;

import com.xixi.annotation.MethodPurpose;
import com.xixi.context.OperationLogTraceContext;
import com.xixi.entity.OperationLog;
import com.xixi.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 管理域操作日志写入组件。
 */
@Component
@RequiredArgsConstructor
public class AdminOperationLogger {
    private final OperationLogMapper operationLogMapper;

    @MethodPurpose("写入管理域操作日志")
    public void log(
            Long userId,
            String userName,
            String userRole,
            String operationType,
            String operationDescription,
            String requestMethod,
            String requestUrl,
            String requestParams,
            String responseResult,
            String ipAddress,
            String userAgent,
            Integer executeTime,
            Boolean status,
            String errorMessage
    ) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUserName(userName);
        log.setUserRole(userRole);
        log.setOperationType(operationType);
        log.setOperationDescription(operationDescription);
        log.setRequestMethod(requestMethod);
        log.setRequestUrl(requestUrl);
        log.setRequestParams(trimToNull(requestParams));
        log.setResponseResult(trimToNull(responseResult));
        log.setIpAddress(trimToNull(ipAddress));
        log.setUserAgent(trimToNull(userAgent));
        log.setExecuteTime(executeTime == null ? 0 : executeTime);
        log.setStatus(status == null ? Boolean.TRUE : status);
        log.setErrorMessage(trimToNull(errorMessage));
        log.setCreatedTime(LocalDateTime.now());
        operationLogMapper.insert(log);
        OperationLogTraceContext.markManualLogged();
    }

    @MethodPurpose("字符串去空并返回 null")
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
