package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投递详情证书项视图对象。
 */
@Data
public class TalentApplicationCertificateVo {
    private Long certificateId;
    private String certificateName;
    private String certificateNumber;
    private String status;
    private Boolean verified;
    private LocalDateTime verifiedTime;
}
