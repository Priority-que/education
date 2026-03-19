package com.xixi.pojo.dto.talent;

import lombok.Data;

/**
 * 按二维码验证参数。
 */
@Data
public class CertificateVerifyByQrcodeDto {
    private String shareToken;
}
