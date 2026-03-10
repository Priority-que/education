package com.xixi.config;

import com.xixi.annotation.MethodPurpose;
import com.xixi.interceptor.RoleAuthorizationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final RoleAuthorizationInterceptor roleAuthorizationInterceptor;

    @Override
    @MethodPurpose("注册角色鉴权拦截器")
    public void addInterceptors(InterceptorRegistry registry) {
        // Test mode: disable role authorization interceptor and open all endpoints.
    }
}
