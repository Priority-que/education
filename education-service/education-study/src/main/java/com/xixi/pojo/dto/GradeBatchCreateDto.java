package com.xixi.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * 教师端：批量录入成绩DTO
 */
@Data
public class GradeBatchCreateDto {

    /**
     * 成绩列表
     */
    private List<GradeCreateDto> gradeList;
}

