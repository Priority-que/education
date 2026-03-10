package com.xixi.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 作业批改DTO。
 */
@Data
public class HomeworkSubmissionGradeDto {

    /**
     * 提交记录ID。
     */
    private Long submissionId;

    /**
     * 批改教师ID。
     */
    private Long teacherId;

    /**
     * 得分。
     */
    private BigDecimal score;

    /**
     * 批改评语。
     */
    private String feedback;

    /**
     * 批注模式：TEXT / RICH_TEXT / STRUCTURED。
     */
    private String annotationMode;

    /**
     * 批注文本。
     */
    private String annotationContent;

    /**
     * 结构化批注 JSON。
     */
    private String annotationDataJson;

    /**
     * 批注附件 URL 列表。
     */
    private List<String> annotationAttachments;
}
