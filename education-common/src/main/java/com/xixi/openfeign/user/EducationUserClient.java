package com.xixi.openfeign.user;

import com.xixi.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * education-user 用户相关远程调用。
 */
@FeignClient(name = "education-user", contextId = "educationUserClient")
public interface EducationUserClient {

    /**
     * 根据用户ID查询用户信息。
     */
    @GetMapping("/users/getUserById/{id}")
    Result getUserById(@PathVariable("id") Long id);
}
