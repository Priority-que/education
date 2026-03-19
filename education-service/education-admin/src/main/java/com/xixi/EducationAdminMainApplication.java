package com.xixi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 管理服务启动类。
 */
@SpringBootApplication
@EnableFeignClients
@MapperScan("com.xixi.mapper")
public class EducationAdminMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(EducationAdminMainApplication.class, args);
    }
}
