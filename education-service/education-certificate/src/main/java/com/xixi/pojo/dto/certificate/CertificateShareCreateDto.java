package com.xixi.pojo.dto.certificate;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 6.1 创建证书分享 DTO。
 */
@Data
public class CertificateShareCreateDto {
    private Long certificateId;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryTime;
}

