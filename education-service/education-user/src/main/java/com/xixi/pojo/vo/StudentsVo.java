package com.xixi.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudentsVo {
    /**
     * 学生ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 学号
     */
    private String studentNumber;

    /**
     * 学校
     */
    private String school;

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

    /**
     * 预计毕业时间
     */
    private String expectedGraduation;

    /**
     * GPA
     */
    private BigDecimal gpa;

    /**
     * 总学分
     */
    private Integer totalCredits;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;
}
