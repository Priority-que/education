package com.xixi.interceptor;

import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 基于网关透传角色头的权限拦截器。
 */
@Component
public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    @Override
    @MethodPurpose("在请求进入控制器前校验角色权限")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RoleRequired roleRequired = handlerMethod.getMethodAnnotation(RoleRequired.class);
        if (roleRequired == null) {
            roleRequired = handlerMethod.getBeanType().getAnnotation(RoleRequired.class);
        }
        if (roleRequired == null) {
            return true;
        }

        Integer role = parseRole(request.getHeader(AuthHeaderConstants.HEADER_USER_ROLE));
        if (role == null) {
            throw new BizException(401, "未登录或用户角色缺失");
        }

        for (int allowRole : roleRequired.value()) {
            if (allowRole == role) {
                return true;
            }
        }
        throw new BizException(403, "无权限访问该资源");
    }

    @MethodPurpose("将请求头角色值解析为整型")
    private Integer parseRole(String roleHeader) {
        if (!StringUtils.hasText(roleHeader)) {
            return null;
        }
        try {
            return Integer.parseInt(roleHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

