package com.xixi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 人才服务主启动类。
 */
@SpringBootApplication
@EnableFeignClients
@MapperScan("com.xixi.mapper")
public class EducationTalentMainApplication {

    /**
     * 启动人才服务应用。
     */
    public static void main(String[] args) {
        SpringApplication.run(EducationTalentMainApplication.class, args);
    }
}
