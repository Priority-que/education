package com.xixi.pojo.query;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentsQuery {
    private Integer pageNum =1;
    private Integer pageSize =10;
    /**
     * 学号
     */
    private String studentNumber;
    /**
     * 学院
     */
    private String college;
    /**
     * 专业
     */
    private String major;
    /**
     * 入学年份
     */
    private String enrollmentYear;
}
