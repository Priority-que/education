package com.xixi.pojo.vo.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投递时间线节点视图对象。
 */
@Data
public class JobApplicationTimelineVo {
    private String nodeCode;
    private String nodeName;
    private String nodeStatus;
    private LocalDateTime nodeTime;
    private String description;
}
