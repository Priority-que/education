package com.xixi.openfeign.user;

import com.xixi.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务企业信息远程调用接口。
 */
@FeignClient(name = "education-user", contextId = "educationUserEnterpriseClient")
public interface EducationUserEnterpriseClient {

    /**
     * 按用户ID查询企业信息。
     */
    @GetMapping("/enterprises/getEnterpriseByUserId/{userId}")
    Result getEnterpriseByUserId(@PathVariable("userId") Long userId);
}
