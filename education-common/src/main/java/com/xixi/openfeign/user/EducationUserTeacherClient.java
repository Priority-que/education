package com.xixi.openfeign.user;

import com.xixi.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * education-user 教师相关远程调用
 */
@FeignClient(name = "education-user", contextId = "educationUserTeacherClient")
public interface EducationUserTeacherClient {

    /**
     * 根据教师姓名获取教师ID
     */
    @GetMapping("/teachers/getTeachersIdByName")
    Result getTeachersIdByName(@RequestParam("name") String name);

    @GetMapping("/teachers/getTeachersNameById/{id}")
    Result getTeachersNameById(@PathVariable Long id);

    @GetMapping("/teachers/getTeacherIdByUserId/{userId}")
    Result getTeacherIdByUserId(@PathVariable("userId") Long userId);
}


