package com.xixi.pojo.vo.certificate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 管理端证书上链分页项。
 */
@Data
public class CertificateAdminBlockchainPageVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long certificateId;
    private String certificateNumber;
    private String status;
    private Long blockHeight;
    private String transactionHash;
    private Boolean anchored;
}

