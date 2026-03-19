package com.xixi.pojo.dto.talent;

import lombok.Data;

import java.util.List;

/**
 * 批量证书验证参数。
 */
@Data
public class CertificateVerifyBatchDto {
    private List<String> certificateNumbers;
}
