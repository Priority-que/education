package com.xixi.pojo.dto.admin;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 内部上报日统计快照请求参数。
 */
@Data
public class AdminInternalStatDailyDto {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate statDate;
    private Integer totalUsers;
    private Integer newUsers;
    private Integer activeUsers;
    private Integer totalStudents;
    private Integer totalTeachers;
    private Integer totalEnterprises;
    private Integer totalCourses;
    private Integer newCourses;
    private Integer totalCertificates;
    private Integer newCertificates;
    private Integer totalResumes;
    private Integer totalJobs;
    private Long totalStudyTime;
}
