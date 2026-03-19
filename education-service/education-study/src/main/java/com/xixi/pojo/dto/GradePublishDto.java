package com.xixi.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * 教师端：发布成绩DTO
 */
@Data
public class GradePublishDto {

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 成绩ID列表
     */
    private List<Long> gradeIds;
}

