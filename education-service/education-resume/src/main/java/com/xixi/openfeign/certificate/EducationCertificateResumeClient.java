package com.xixi.openfeign.certificate;

import com.xixi.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 证书服务简历域远程调用接口。
 */
@FeignClient(name = "education-certificate", contextId = "educationCertificateResumeClient")
public interface EducationCertificateResumeClient {

    /**
     * 调用证书服务内部接口：按学生查询证书列表。
     */
    @GetMapping("/certificate/internal/student/list")
    Result listStudentCertificates(
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "status", required = false) String status
    );
}
