package com.xixi.pojo.vo.certificate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.xixi.entity.BlockchainRecord;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 5.2/5.3 证书详情。
 */
@Data
public class CertificateDetailVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String certificateNumber;
    private Long studentId;
    private Long courseId;
    private Long teacherId;
    private String certificateName;
    private String issuingAuthority;
    private LocalDate issuingDate;
    private LocalDate expiryDate;
    private String certificateContent;
    private String metadataJson;
    private String fileUrl;
    private String thumbnailUrl;
    private String hash;
    private String previousHash;
    private String status;
    private Long blockHeight;
    private String transactionHash;
    private Integer verificationCount;
    private LocalDateTime lastVerificationTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private BlockchainRecord blockchainRecord;
}
