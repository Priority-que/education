package com.xixi.openfeign.resume;

import com.xixi.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 简历服务人才域远程调用接口。
 */
@FeignClient(name = "education-resume", contextId = "educationResumeTalentClient")
public interface EducationResumeTalentClient {

    /**
     * 调用简历服务内部分页接口。
     */
    @GetMapping("/resume/internal/public/student/page")
    Result getInternalPublicStudentPage(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "major", required = false) String major,
            @RequestParam(value = "degree", required = false) String degree,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    );

    /**
     * 调用简历服务内部详情接口。
     */
    @GetMapping("/resume/internal/public/student/{studentId}")
    Result getInternalPublicStudentDetail(
            @PathVariable("studentId") Long studentId,
            @RequestParam(value = "resumeId", required = false) Long resumeId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    );
}
