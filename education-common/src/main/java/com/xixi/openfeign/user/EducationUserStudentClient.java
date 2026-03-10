package com.xixi.openfeign.user;

import com.xixi.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * education-user 学生相关远程调用
 */
@FeignClient(name = "education-user", contextId = "educationUserStudentClient")
public interface EducationUserStudentClient {

    /**
     * 根据学生ID获取学生信息
     */
    @GetMapping("/students/getStudentById/{id}")
    Result getStudentById(@PathVariable("id") Long id);

    /**
     * 根据用户ID获取学生信息
     */
    @GetMapping("/students/getStudentByUserId/{userId}")
    Result getStudentByUserId(@PathVariable("userId") Long userId);
}
