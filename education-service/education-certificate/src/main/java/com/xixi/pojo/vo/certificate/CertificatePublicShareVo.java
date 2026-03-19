package com.xixi.pojo.vo.certificate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 6.4 公开分享证书视图。
 */
@Data
public class CertificatePublicShareVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shareId;
    private String shareToken;
    private String shareUrl;
    private LocalDateTime expiryTime;
    private Integer viewCount;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long certificateId;
    private String certificateNumber;
    private String certificateName;
    private String issuingAuthority;
    private LocalDate issuingDate;
    private LocalDate expiryDate;
    private String status;
    private String thumbnailUrl;
    private String fileUrl;
    private String hash;
    private Long blockHeight;
    private String transactionHash;
}
