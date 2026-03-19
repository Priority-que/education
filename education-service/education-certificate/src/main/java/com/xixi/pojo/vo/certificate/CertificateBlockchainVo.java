package com.xixi.pojo.vo.certificate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 9.1 证书区块链存证信息。
 */
@Data
public class CertificateBlockchainVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long certificateId;
    private String certificateNumber;
    private String certificateStatus;
    private LocalDate issuingDate;
    private String certificateHash;
    private String certificatePreviousHash;
    private Long blockHeight;
    private String transactionHash;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long blockchainRecordId;
    private String recordCurrentHash;
    private String recordPreviousHash;
    private String merkleRoot;
    private Long timestamp;
    private Long nonce;
    private Integer blockSize;
    private Integer transactionCount;
    private LocalDateTime blockchainCreatedTime;
}
