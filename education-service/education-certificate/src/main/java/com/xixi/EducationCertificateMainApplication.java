package com.xixi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 证书服务主启动类。
 */
@SpringBootApplication
@MapperScan("com.xixi.mapper")
@EnableFeignClients(basePackages = "com.xixi.openfeign")
public class EducationCertificateMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(EducationCertificateMainApplication.class, args);
    }
}
