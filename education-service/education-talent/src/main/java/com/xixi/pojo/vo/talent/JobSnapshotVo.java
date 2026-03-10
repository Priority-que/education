package com.xixi.pojo.vo.talent;

import lombok.Data;

/**
 * 岗位快照视图对象。
 */
@Data
public class JobSnapshotVo {
    private Long jobId;
    private String jobTitle;
    private String jobType;
    private String jobCategory;
    private String workLocation;
    private String salaryRange;
}
