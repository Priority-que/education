package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位投递记录实体。
 */
@Data
@TableName("job_application")
public class JobApplication {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String applicationNo;

    private Long jobId;

    private Long studentId;

    private Long resumeId;

    private Long enterpriseId;

    private String status;

    private String sourceType;

    private Integer matchScore;

    private Long latestCommunicationId;

    private String latestCommunicationType;

    private LocalDateTime latestCommunicationTime;

    private Boolean readByStudent;

    private Boolean readByEnterprise;

    private String jobSnapshotJson;

    private String enterpriseSnapshotJson;

    private String resumeSnapshotJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime submittedTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    private String remark;
}
