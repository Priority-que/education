package com.xixi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 学习服务主启动类
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.xixi.openfeign")
public class EducationStudyMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(EducationStudyMainApplication.class, args);
    }
}
