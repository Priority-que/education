package com.xixi.pojo.dto.certificate;

import lombok.Data;

import java.util.List;

/**
 * 7.3 批量验证 DTO。
 */
@Data
public class CertificateVerifyBatchDto {
    private List<String> certificateNumbers;
}

