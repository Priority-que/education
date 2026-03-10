package com.xixi.pojo.vo.talent;

import lombok.Data;

/**
 * 企业快照视图对象。
 */
@Data
public class EnterpriseSnapshotVo {
    private Long enterpriseId;
    private String enterpriseName;
    private String enterpriseLogo;
    private String industry;
    private String city;
    private Boolean enterpriseVerified;
    private String enterpriseIntroduction;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
}
