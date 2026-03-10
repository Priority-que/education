package com.xixi.pojo.query;

import lombok.Data;

@Data
public class TeachersQuery {
    private Integer pageNum;
    private Integer pageSize;
    /**
     * 教师工号
     */
    private String teacherNumber;
    /**
     * 职称
     */
    private String title;
    /**
     * 部门
     */
    private String department;
}
