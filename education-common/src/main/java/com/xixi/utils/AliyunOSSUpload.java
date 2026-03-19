package com.xixi.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyuncs.exceptions.ClientException;
import com.xixi.config.AliyunOSSConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
public class AliyunOSSUpload {

    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");

    @Autowired
    private AliyunOSSConfig aliyunOSSConfig;

    public String upload(String fileName, byte[] bytes) throws ClientException {
        return upload("common", fileName, bytes);
    }

    public String upload(String bizDir, String fileName, byte[] bytes) throws ClientException {
        String endpoint = aliyunOSSConfig.getEndpoint();
        String bucketName = aliyunOSSConfig.getBucketName();
        String region = aliyunOSSConfig.getRegion();
        String accessKeyId = aliyunOSSConfig.getAccessKeyId();
        String accessKeySecret = aliyunOSSConfig.getAccessKeySecret();

        // 使用配置文件中的密钥创建凭证提供者
        DefaultCredentialProvider credentialsProvider = CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId, accessKeySecret);
        String dir = normalizeBizDir(bizDir) + "/" + LocalDate.now().format(DATE_PATH_FORMATTER);
        String newFileName = UUID.randomUUID() + extractFileExtension(fileName);
        String objectName = dir + "/" + newFileName;

        // 创建OSSClient实例。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } finally {
            ossClient.shutdown();
        }
        return buildFileUrl(endpoint, bucketName, objectName);
    }

    private String normalizeBizDir(String bizDir) {
        if (bizDir == null || bizDir.isBlank()) {
            return "common";
        }
        String normalized = bizDir.trim().replace('\\', '/');
        normalized = normalized.replaceAll("^/+", "").replaceAll("/+$", "");
        normalized = normalized.replaceAll("[^a-zA-Z0-9/_-]", "");
        return normalized.isBlank() ? "common" : normalized;
    }

    private String extractFileExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex);
    }

    private String buildFileUrl(String endpoint, String bucketName, String objectName) {
        String normalizedEndpoint = endpoint == null ? "" : endpoint.trim();
        if (normalizedEndpoint.endsWith("/")) {
            normalizedEndpoint = normalizedEndpoint.substring(0, normalizedEndpoint.length() - 1);
        }
        String[] endpointParts = normalizedEndpoint.split("://", 2);
        if (endpointParts.length == 2) {
            return endpointParts[0] + "://" + bucketName + "." + endpointParts[1] + "/" + objectName;
        }
        return "https://" + bucketName + "." + normalizedEndpoint + "/" + objectName;
    }
}
