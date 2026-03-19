package com.xixi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.xixi.openfeign")
public class EducationCourseMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(EducationCourseMainApplication.class, args);
    }
}
