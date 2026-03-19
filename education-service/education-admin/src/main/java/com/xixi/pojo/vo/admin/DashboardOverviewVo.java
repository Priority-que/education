package com.xixi.pojo.vo.admin;

import lombok.Data;

/**
 * 看板总览视图对象。
 */
@Data
public class DashboardOverviewVo {
    private Integer totalUsers;
    private Integer totalStudents;
    private Integer totalTeachers;
    private Integer totalEnterprises;
    private Integer totalCourses;
    private Integer totalCertificates;
    private Integer totalResumes;
    private Integer totalJobs;
    private Integer activeUsers;
}
