package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板触发日志实体。
 */
@Data
@TableName("message_template_trigger_log")
public class MessageTemplateTriggerLog {
    /**
     * 主键ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 事件ID（幂等键）。
     */
    private String eventId;

    /**
     * 事件编码。
     */
    private String eventCode;

    /**
     * 规则ID。
     */
    private Long ruleId;

    /**
     * 模板编码。
     */
    private String templateCode;

    /**
     * 发送状态：SUCCESS/FAILED/SKIPPED。
     */
    private String sendStatus;

    /**
     * 失败原因。
     */
    private String errorMessage;

    /**
     * 创建时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}

