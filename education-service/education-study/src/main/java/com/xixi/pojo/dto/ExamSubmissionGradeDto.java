package com.xixi.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 教师端批改测验提交DTO。
 */
@Data
public class ExamSubmissionGradeDto {

    /**
     * 提交ID。
     */
    private Long submissionId;

    /**
     * 教师ID。
     */
    private Long teacherId;

    /**
     * 主观题得分映射（key=题目ID，value=该题得分）。
     */
    private Map<Long, BigDecimal> essayScoreMap;
}
