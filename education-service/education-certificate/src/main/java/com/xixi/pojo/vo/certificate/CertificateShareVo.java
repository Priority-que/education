package com.xixi.pojo.vo.certificate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 6.3 我的证书分享分页项。
 */
@Data
public class CertificateShareVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long certificateId;
    private String certificateNumber;
    private String certificateName;
    private String shareToken;
    private String shareUrl;
    private String qrCodeUrl;
    private LocalDateTime expiryTime;
    private Integer viewCount;
    private Boolean isActive;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
