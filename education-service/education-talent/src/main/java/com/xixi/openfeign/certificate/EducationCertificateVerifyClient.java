package com.xixi.openfeign.certificate;

import com.xixi.pojo.dto.talent.CertificateVerifyBatchDto;
import com.xixi.pojo.dto.talent.CertificateVerifyByNumberDto;
import com.xixi.pojo.dto.talent.CertificateVerifyByQrcodeDto;
import com.xixi.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 证书验证服务远程调用接口。
 */
@FeignClient(name = "education-certificate", contextId = "educationCertificateVerifyClient")
public interface EducationCertificateVerifyClient {

    /**
     * 调用证书服务编号验证接口。
     */
    @PostMapping("/certificate/verify/number")
    Result verifyByNumber(
            @RequestBody CertificateVerifyByNumberDto dto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    );

    /**
     * 调用证书服务二维码验证接口。
     */
    @PostMapping("/certificate/verify/qrcode")
    Result verifyByQrcode(
            @RequestBody CertificateVerifyByQrcodeDto dto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    );

    /**
     * 调用证书服务批量验证接口。
     */
    @PostMapping("/certificate/verify/batch")
    Result verifyBatch(
            @RequestBody CertificateVerifyBatchDto dto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    );

    /**
     * 调用证书服务验证历史接口。
     */
    @GetMapping("/certificate/verify/history")
    Result getVerifyHistory(
            @RequestParam("pageNum") Long pageNum,
            @RequestParam("pageSize") Long pageSize,
            @RequestParam(value = "verificationResult", required = false) String verificationResult,
            @RequestParam(value = "verificationMethod", required = false) String verificationMethod,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    );

    /**
     * 调用证书服务验证报告接口。
     */
    @GetMapping("/certificate/verify/report/{verificationId}")
    Result getVerifyReport(
            @PathVariable("verificationId") Long verificationId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    );
}
