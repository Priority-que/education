package com.xixi.pojo.vo.resume;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生公开简历选项（用于企业在详情页切换查看不同公开简历）。
 */
@Data
public class PublicResumeOptionVo {
    private Long resumeId;
    private String resumeTitle;
    private Boolean isDefault;
    private LocalDateTime updatedTime;
}
