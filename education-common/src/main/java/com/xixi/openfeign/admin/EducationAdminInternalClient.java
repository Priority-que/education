package com.xixi.openfeign.admin;

import com.xixi.openfeign.admin.dto.OperationLogReportRequest;
import com.xixi.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * education-admin 内部审核同步远程调用
 */
@FeignClient(name = "education-admin", contextId = "educationAdminInternalClient")
public interface EducationAdminInternalClient {

    @PostMapping("/admin/internal/audit/create")
    Result createAudit(@RequestBody Map<String, Object> payload);

    @PostMapping("/admin/internal/audit/sync")
    Result syncAudit(@RequestBody Map<String, Object> payload);

    @PostMapping("/admin/internal/log/operation")
    Result reportOperationLog(@RequestBody OperationLogReportRequest payload);
}
