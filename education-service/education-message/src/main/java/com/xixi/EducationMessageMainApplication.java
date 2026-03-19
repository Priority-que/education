package com.xixi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.xixi.openfeign")
@MapperScan("com.xixi.mapper")
public class EducationMessageMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(EducationMessageMainApplication.class, args);
    }
}
