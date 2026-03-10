package com.xixi.pojo.vo.talent;

import lombok.Data;

/**
 * 企业端投递详情学生信息视图对象。
 */
@Data
public class TalentApplicationStudentInfoVo {
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private String avatarUrl;
    private String major;
    private String degree;
    private String schoolName;
}
