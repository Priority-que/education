package com.xixi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 简历服务主启动类。
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.xixi.openfeign")
@MapperScan("com.xixi.mapper")
public class EducationResumeMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(EducationResumeMainApplication.class, args);
    }
}
