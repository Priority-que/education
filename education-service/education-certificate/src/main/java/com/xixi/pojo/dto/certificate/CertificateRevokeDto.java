package com.xixi.pojo.dto.certificate;

import lombok.Data;

/**
 * 8.2 教师撤销证书 DTO。
 */
@Data
public class CertificateRevokeDto {
    /**
     * 撤销原因。
     */
    private String revokeReason;
}
