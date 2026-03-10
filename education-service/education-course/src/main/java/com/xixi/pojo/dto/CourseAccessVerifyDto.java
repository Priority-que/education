package com.xixi.pojo.dto;

import lombok.Data;

/**
 * 课程访问密码校验参数。
 */
@Data
public class CourseAccessVerifyDto {
    private Long courseId;
    private String password;
}
