package com.xixi.controller;

import com.xixi.annotation.MethodPurpose;
import com.xixi.pojo.dto.certificate.CertificateInternalIssueFromGradeDto;
import com.xixi.pojo.dto.certificate.CertificateInternalValidateIdsDto;
import com.xixi.pojo.vo.certificate.CertificateInternalStudentVo;
import com.xixi.pojo.vo.certificate.CertificateInternalValidateIdsVo;
import com.xixi.service.CertificateInternalService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 证书内部接口（10.1~10.3）。
 */
@RestController
@RequestMapping("/certificate/internal")
@RequiredArgsConstructor
public class CertificateInternalController {
    private final CertificateInternalService certificateInternalService;

    @MethodPurpose("10.1：成绩达标触发发证（内部）")
    @PostMapping("/issue/from-grade")
    public Result issueFromGrade(@RequestBody CertificateInternalIssueFromGradeDto dto) {
        return certificateInternalService.issueFromGrade(dto);
    }

    @MethodPurpose("10.2：查询学生证书列表（内部）")
    @GetMapping("/student/list")
    public Result listStudentCertificates(
            @RequestParam Long studentId,
            @RequestParam(required = false) String status
    ) {
        List<CertificateInternalStudentVo> list = certificateInternalService.listStudentCertificates(studentId, status);
        return Result.success(list);
    }

    @MethodPurpose("10.3：批量校验证书ID合法性（内部）")
    @PostMapping("/validate/ids")
    public Result validateCertificateIds(@RequestBody CertificateInternalValidateIdsDto dto) {
        CertificateInternalValidateIdsVo vo = certificateInternalService.validateCertificateIds(dto);
        return Result.success(vo);
    }
}

