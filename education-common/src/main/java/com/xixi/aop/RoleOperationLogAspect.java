package com.xixi.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xixi.context.OperationLogTraceContext;
import com.xixi.openfeign.admin.EducationAdminInternalClient;
import com.xixi.openfeign.admin.dto.OperationLogReportRequest;
import com.xixi.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 全角色操作日志自动埋点切面。
 * 统计规则：
 * 1. 仅统计 Controller 层请求；
 * 2. 排除 GET 请求；
 * 3. 排除 /admin/internal/** 与其他 /internal/** 请求；
 * 4. 仅统计已识别角色（管理员/学生/教师/企业）；
 * 5. 若业务已手工写入日志，则本次请求不重复上报。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RoleOperationLogAspect {
    private static final Logger log = LoggerFactory.getLogger(RoleOperationLogAspect.class);
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final int MAX_TEXT_LENGTH = 2000;
    private static final String ROLE_REQUIRED_ANNOTATION = "com.xixi.annotation.RoleRequired";
    private static final String METHOD_PURPOSE_ANNOTATION = "com.xixi.annotation.MethodPurpose";
    private static final Map<Integer, String> ROLE_NAME_MAP = Map.of(
            1, "ADMIN",
            2, "STUDENT",
            3, "TEACHER",
            4, "ENTERPRISE"
    );

    private final EducationAdminInternalClient educationAdminInternalClient;
    private final ObjectMapper objectMapper;

    @Around("execution(public * com.xixi.controller..*(..))")
    public Object recordRoleOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = currentRequest();
        if (request == null || !shouldRecord(request)) {
            return joinPoint.proceed();
        }

        Method targetMethod = resolveTargetMethod(joinPoint);
        String roleName = resolveRoleName(request, targetMethod);
        if (!StringUtils.hasText(roleName)) {
            return joinPoint.proceed();
        }

        OperationLogTraceContext.clear();
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable throwable = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            throwable = ex;
            throw ex;
        } finally {
            try {
                if (!OperationLogTraceContext.isManualLogged()) {
                    reportOperation(request, joinPoint, targetMethod, roleName, result, throwable, System.currentTimeMillis() - start);
                }
            } catch (Exception reportEx) {
                log.warn("operation log report failed, uri={}", request.getRequestURI(), reportEx);
            } finally {
                OperationLogTraceContext.clear();
            }
        }
    }

    private boolean shouldRecord(HttpServletRequest request) {
        String httpMethod = request.getMethod();
        if (!StringUtils.hasText(httpMethod)) {
            return false;
        }
        if (HttpMethod.GET.matches(httpMethod)) {
            return false;
        }

        String uri = request.getRequestURI();
        if (!StringUtils.hasText(uri)) {
            return false;
        }
        return !uri.startsWith("/admin/internal/") && !uri.contains("/internal/");
    }

    private void reportOperation(
            HttpServletRequest request,
            ProceedingJoinPoint joinPoint,
            Method targetMethod,
            String roleName,
            Object result,
            Throwable throwable,
            long elapsedMillis
    ) {
        OperationLogReportRequest payload = new OperationLogReportRequest();
        Long userId = parseLong(request.getHeader(HEADER_USER_ID));
        payload.setUserId(userId);
        payload.setUserName(buildUserName(request.getHeader(HEADER_USERNAME), roleName, userId));
        payload.setUserRole(roleName);
        payload.setOperationType(buildOperationType(request.getMethod(), roleName, targetMethod));
        payload.setOperationDescription(buildOperationDescription(request, targetMethod));
        payload.setRequestMethod(request.getMethod());
        payload.setRequestUrl(buildRequestUrl(request));
        payload.setRequestParams(buildRequestParams(request, joinPoint.getArgs()));
        payload.setResponseResult(buildResponseResult(result));
        payload.setIpAddress(resolveClientIp(request));
        payload.setUserAgent(truncate(request.getHeader(HEADER_USER_AGENT)));
        payload.setExecuteTime(toInt(elapsedMillis));
        payload.setStatus(isSuccess(result, throwable));
        payload.setErrorMessage(resolveErrorMessage(result, throwable));
        educationAdminInternalClient.reportOperationLog(payload);
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private Method resolveTargetMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        try {
            return joinPoint.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return method;
        }
    }

    private String resolveRoleName(HttpServletRequest request, Method targetMethod) {
        String roleHeader = request.getHeader(HEADER_USER_ROLE);
        String byHeader = normalizeRoleName(roleHeader);
        if (StringUtils.hasText(byHeader)) {
            return byHeader;
        }
        return resolveRoleNameFromAnnotation(targetMethod);
    }

    private String resolveRoleNameFromAnnotation(Method targetMethod) {
        if (targetMethod == null) {
            return null;
        }
        String fromMethod = extractRoleName(targetMethod.getAnnotations());
        if (StringUtils.hasText(fromMethod)) {
            return fromMethod;
        }
        return extractRoleName(targetMethod.getDeclaringClass().getAnnotations());
    }

    private String extractRoleName(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (!ROLE_REQUIRED_ANNOTATION.equals(type.getName())) {
                continue;
            }
            try {
                Object value = type.getMethod("value").invoke(annotation);
                if (value instanceof int[] roles && roles.length > 0) {
                    return ROLE_NAME_MAP.get(roles[0]);
                }
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private String buildOperationType(String httpMethod, String roleName, Method targetMethod) {
        String controllerName = targetMethod == null
                ? "UNKNOWN"
                : targetMethod.getDeclaringClass().getSimpleName().replace("Controller", "").toUpperCase(Locale.ROOT);
        String methodName = targetMethod == null ? "UNKNOWN" : targetMethod.getName().toUpperCase(Locale.ROOT);
        String safeHttpMethod = StringUtils.hasText(httpMethod) ? httpMethod.toUpperCase(Locale.ROOT) : "UNKNOWN";
        return truncate("AUTO_" + safeHttpMethod + "_" + roleName + "_" + controllerName + "_" + methodName);
    }

    private String buildOperationDescription(HttpServletRequest request, Method targetMethod) {
        String purpose = resolveMethodPurpose(targetMethod);
        if (StringUtils.hasText(purpose)) {
            return truncate(purpose);
        }
        return truncate(request.getMethod() + " " + request.getRequestURI());
    }

    private String resolveMethodPurpose(Method targetMethod) {
        if (targetMethod == null) {
            return null;
        }
        String fromMethod = extractMethodPurpose(targetMethod.getAnnotations());
        if (StringUtils.hasText(fromMethod)) {
            return fromMethod;
        }
        return extractMethodPurpose(targetMethod.getDeclaringClass().getAnnotations());
    }

    private String extractMethodPurpose(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (!METHOD_PURPOSE_ANNOTATION.equals(type.getName())) {
                continue;
            }
            try {
                Object value = type.getMethod("value").invoke(annotation);
                if (value instanceof String text && StringUtils.hasText(text)) {
                    return text.trim();
                }
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private String buildRequestUrl(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (!StringUtils.hasText(queryString)) {
            return truncate(request.getRequestURI());
        }
        return truncate(request.getRequestURI() + "?" + queryString);
    }

    private String buildRequestParams(HttpServletRequest request, Object[] args) {
        if (args == null || args.length == 0) {
            return truncate(request.getQueryString());
        }
        List<Object> serializableArgs = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null || isIgnoredArg(arg)) {
                continue;
            }
            serializableArgs.add(arg);
        }
        if (serializableArgs.isEmpty()) {
            return truncate(request.getQueryString());
        }
        Object payload = serializableArgs.size() == 1 ? serializableArgs.get(0) : serializableArgs;
        return truncate(toJson(payload));
    }

    private String buildResponseResult(Object result) {
        if (result == null) {
            return null;
        }
        return truncate(toJson(result));
    }

    private boolean isIgnoredArg(Object arg) {
        return arg instanceof HttpServletRequest
                || arg instanceof HttpServletResponse
                || arg instanceof BindingResult
                || arg instanceof MultipartFile
                || arg instanceof MultipartFile[]
                || arg.getClass().getName().startsWith("org.springframework.web.multipart");
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            String[] parts = xForwardedFor.split(",");
            if (parts.length > 0 && StringUtils.hasText(parts[0])) {
                return truncate(parts[0].trim());
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return truncate(realIp.trim());
        }
        return truncate(request.getRemoteAddr());
    }

    private String buildUserName(String usernameHeader, String roleName, Long userId) {
        if (StringUtils.hasText(usernameHeader)) {
            return truncate(usernameHeader.trim());
        }
        if (userId == null) {
            return truncate(roleName);
        }
        return truncate(roleName + userId);
    }

    private boolean isSuccess(Object result, Throwable throwable) {
        if (throwable != null) {
            return false;
        }
        if (result instanceof Result response) {
            return response.getCode() != null && response.getCode() == 200;
        }
        return true;
    }

    private String resolveErrorMessage(Object result, Throwable throwable) {
        if (throwable != null) {
            return truncate(throwable.getMessage());
        }
        if (result instanceof Result response && (response.getCode() == null || response.getCode() != 200)) {
            return truncate(response.getMessage());
        }
        return null;
    }

    private String normalizeRoleName(String roleHeader) {
        if (!StringUtils.hasText(roleHeader)) {
            return null;
        }
        String trimmed = roleHeader.trim().toUpperCase(Locale.ROOT);
        Integer roleCode = parseInteger(trimmed);
        if (roleCode != null) {
            return ROLE_NAME_MAP.get(roleCode);
        }
        return switch (trimmed) {
            case "ADMIN", "ROLE_ADMIN" -> "ADMIN";
            case "STUDENT", "ROLE_STUDENT" -> "STUDENT";
            case "TEACHER", "ROLE_TEACHER" -> "TEACHER";
            case "ENTERPRISE", "ROLE_ENTERPRISE", "COMPANY" -> "ENTERPRISE";
            default -> null;
        };
    }

    private Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int toInt(long value) {
        if (value <= 0) {
            return 0;
        }
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }

    private String truncate(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= MAX_TEXT_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_TEXT_LENGTH);
    }
}

