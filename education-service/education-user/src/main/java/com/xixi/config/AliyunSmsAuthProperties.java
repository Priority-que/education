package com.xixi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "auth.sms-auth.aliyun")
public class AliyunSmsAuthProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String regionId = "cn-hangzhou";
    private String endpoint = "dypnsapi.aliyuncs.com";
    private String countryCode = "86";
    private String schemeName;
    private String signName;
    private String templateCode;
    private String templateParam = "{\"code\":\"##code##\",\"min\":\"5\"}";
    private Boolean useCodeType = true;
    private Integer codeType = 1;
    private Integer codeLength = 6;
    private Integer interval;
    private Integer validTime;
    private Integer duplicatePolicy;
    private Boolean returnVerifyCode;
    private Integer connectTimeoutMs = 5000;
    private Integer readTimeoutMs = 5000;
    private Integer defaultRole = 2;
    private String usernamePrefix = "u";
}
