package com.xixi.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * 教师端：撤销成绩发布DTO
 */
@Data
public class GradeUnpublishDto {

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 成绩ID列表
     */
    private List<Long> gradeIds;
}

