package com.xixi.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板触发规则实体。
 */
@Data
@TableName("message_template_trigger_rule")
public class MessageTemplateTriggerRule {
    /**
     * 主键ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则编码。
     */
    private String ruleCode;

    /**
     * 事件编码。
     */
    private String eventCode;

    /**
     * 模板编码。
     */
    private String templateCode;

    /**
     * 覆盖消息类型。
     */
    private String messageType;

    /**
     * 投递模式：SYNC/MQ。
     */
    private String deliverMode;

    /**
     * 覆盖优先级：0/1/2。
     */
    private Integer priority;

    /**
     * 覆盖关联类型。
     */
    private String relatedType;

    /**
     * 状态：0-禁用，1-启用。
     */
    private Boolean status;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 创建时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}

