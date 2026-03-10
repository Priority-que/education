package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam_question")
public class ExamQuestion {
    
    /**
     * 试题ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 测验ID
     */
    private Long examId;
    
    /**
     * 题目类型: SINGLE_CHOICE-单选, MULTI_CHOICE-多选, TRUE_FALSE-判断, FILL_BLANK-填空, ESSAY-问答
     */
    private String questionType;
    
    /**
     * 题目内容
     */
    private String questionContent;
    
    /**
     * 选项(JSON数组)
     */
    private String options;
    
    /**
     * 正确答案
     */
    private String correctAnswer;
    
    /**
     * 分值
     */
    private Integer score;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}

