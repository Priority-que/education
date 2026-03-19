package com.xixi.pojo.vo.talent;

import lombok.Data;

/**
 * 岗位详情推荐简历项视图对象。
 */
@Data
public class JobPublicResumeOptionVo {
    private Long resumeId;
    private String resumeTitle;
    private Boolean isDefault;
    private String visibility;
    private Integer completeScore;
    private Boolean canApply;
    private String disableReason;
}
