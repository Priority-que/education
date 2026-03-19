package com.xixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.entity.MessageTemplate;
import com.xixi.exception.BizException;
import com.xixi.mapper.MessageTemplateMapper;
import com.xixi.mq.MessageTemplateEventProducer;
import com.xixi.pojo.dto.message.MessageTemplateCreateDto;
import com.xixi.pojo.dto.message.MessageTemplateUpdateDto;
import com.xixi.pojo.query.message.MessageTemplateQuery;
import com.xixi.pojo.vo.message.MessageTemplateDetailVo;
import com.xixi.service.MessageTemplateAdminService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 管理员模板管理服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageTemplateAdminServiceImpl implements MessageTemplateAdminService {
    private static final String TYPE_EMAIL = "EMAIL";
    private static final String TYPE_SMS = "SMS";
    private static final String TYPE_NOTIFICATION = "NOTIFICATION";

    private final MessageTemplateMapper messageTemplateMapper;
    private final MessageTemplateEventProducer messageTemplateEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("模板管理5.1：创建消息模板并发布模板变更事件")
    public Result createTemplate(MessageTemplateCreateDto dto, Long operatorId) {
        validateCreateDto(dto);
        String templateCode = dto.getTemplateCode().trim();

        long exists = messageTemplateMapper.selectCount(
                new LambdaQueryWrapper<MessageTemplate>().eq(MessageTemplate::getTemplateCode, templateCode));
        if (exists > 0) {
            throw new BizException(409, "模板编码已存在");
        }

        MessageTemplate template = new MessageTemplate();
        template.setTemplateCode(templateCode);
        template.setTemplateName(dto.getTemplateName().trim());
        template.setTemplateType(dto.getTemplateType().trim());
        template.setTemplateSubject(trimToNull(dto.getTemplateSubject()));
        template.setTemplateContent(dto.getTemplateContent().trim());
        template.setVariables(toVariablesJson(dto.getVariables()));
        template.setDescription(trimToNull(dto.getDescription()));
        template.setStatus(toStatusBoolean(dto.getStatus(), true));
        template.setCreatedTime(LocalDateTime.now());
        template.setUpdatedTime(LocalDateTime.now());
        messageTemplateMapper.insert(template);

        messageTemplateEventProducer.publish("CREATE", operatorId, template.getId(), template.getTemplateCode());
        return Result.success("创建模板成功", Map.of("id", template.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("模板管理5.2：修改消息模板并发布模板变更事件")
    public Result updateTemplate(MessageTemplateUpdateDto dto, Long operatorId) {
        if (dto == null || dto.getId() == null) {
            throw new BizException(400, "模板ID不能为空");
        }

        MessageTemplate template = requireTemplate(dto.getId());
        boolean updated = false;

        if (StringUtils.hasText(dto.getTemplateName())) {
            template.setTemplateName(dto.getTemplateName().trim());
            updated = true;
        }
        if (StringUtils.hasText(dto.getTemplateType())) {
            validateTemplateType(dto.getTemplateType());
            template.setTemplateType(dto.getTemplateType().trim());
            updated = true;
        }
        if (dto.getTemplateSubject() != null) {
            template.setTemplateSubject(trimToNull(dto.getTemplateSubject()));
            updated = true;
        }
        if (StringUtils.hasText(dto.getTemplateContent())) {
            template.setTemplateContent(dto.getTemplateContent().trim());
            updated = true;
        }
        if (dto.getVariables() != null) {
            template.setVariables(toVariablesJson(dto.getVariables()));
            updated = true;
        }
        if (dto.getDescription() != null) {
            template.setDescription(trimToNull(dto.getDescription()));
            updated = true;
        }
        if (dto.getStatus() != null) {
            template.setStatus(toStatusBoolean(dto.getStatus(), null));
            updated = true;
        }

        if (!updated) {
            throw new BizException(400, "未检测到可更新字段");
        }
        template.setUpdatedTime(LocalDateTime.now());
        messageTemplateMapper.updateById(template);

        messageTemplateEventProducer.publish("UPDATE", operatorId, template.getId(), template.getTemplateCode());
        return Result.success("修改模板成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("模板管理5.3：启用或禁用模板并发布状态变更事件")
    public Result updateTemplateStatus(Long id, Integer status, Long operatorId) {
        MessageTemplate template = requireTemplate(id);
        template.setStatus(toStatusBoolean(status, null));
        template.setUpdatedTime(LocalDateTime.now());
        messageTemplateMapper.updateById(template);

        messageTemplateEventProducer.publish("STATUS_CHANGE", operatorId, template.getId(), template.getTemplateCode());
        return Result.success("模板状态更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("模板管理5.4：删除模板并发布删除事件")
    public Result deleteTemplate(Long id, Long operatorId) {
        MessageTemplate template = requireTemplate(id);
        messageTemplateMapper.deleteById(id);
        messageTemplateEventProducer.publish("DELETE", operatorId, template.getId(), template.getTemplateCode());
        return Result.success("删除模板成功");
    }

    @Override
    @MethodPurpose("模板管理5.5：查询模板详情")
    public MessageTemplateDetailVo getTemplateDetail(Long id) {
        return toDetailVo(requireTemplate(id));
    }

    @Override
    @MethodPurpose("模板管理5.6：分页查询模板列表")
    public IPage<MessageTemplateDetailVo> getTemplatePage(MessageTemplateQuery query) {
        MessageTemplateQuery safeQuery = query == null ? new MessageTemplateQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0 ? 20 : safeQuery.getPageSize();

        LambdaQueryWrapper<MessageTemplate> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(safeQuery.getTemplateCode())) {
            wrapper.like(MessageTemplate::getTemplateCode, safeQuery.getTemplateCode().trim());
        }
        if (StringUtils.hasText(safeQuery.getTemplateType())) {
            wrapper.eq(MessageTemplate::getTemplateType, safeQuery.getTemplateType().trim());
        }
        if (safeQuery.getStatus() != null) {
            wrapper.eq(MessageTemplate::getStatus, toStatusBoolean(safeQuery.getStatus(), null));
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(w -> w.like(MessageTemplate::getTemplateName, keyword)
                    .or()
                    .like(MessageTemplate::getDescription, keyword));
        }
        wrapper.orderByDesc(MessageTemplate::getUpdatedTime, MessageTemplate::getCreatedTime);

        Page<MessageTemplate> entityPage = messageTemplateMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<MessageTemplateDetailVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<MessageTemplateDetailVo> voList = entityPage.getRecords().stream().map(this::toDetailVo).toList();
        voPage.setRecords(voList);
        return voPage;
    }

    @MethodPurpose("校验模板创建参数")
    private void validateCreateDto(MessageTemplateCreateDto dto) {
        if (dto == null) {
            throw new BizException(400, "创建参数不能为空");
        }
        if (!StringUtils.hasText(dto.getTemplateCode())) {
            throw new BizException(400, "模板编码不能为空");
        }
        if (!StringUtils.hasText(dto.getTemplateName())) {
            throw new BizException(400, "模板名称不能为空");
        }
        if (!StringUtils.hasText(dto.getTemplateType())) {
            throw new BizException(400, "模板类型不能为空");
        }
        if (!StringUtils.hasText(dto.getTemplateContent())) {
            throw new BizException(400, "模板内容不能为空");
        }
        validateTemplateType(dto.getTemplateType());
        if (dto.getStatus() != null && dto.getStatus() != 0 && dto.getStatus() != 1) {
            throw new BizException(400, "模板状态只能为0或1");
        }
    }

    @MethodPurpose("校验模板类型是否合法")
    private void validateTemplateType(String templateType) {
        if (!StringUtils.hasText(templateType)) {
            throw new BizException(400, "模板类型不能为空");
        }
        String normalized = templateType.trim();
        if (!Objects.equals(normalized, TYPE_EMAIL)
                && !Objects.equals(normalized, TYPE_SMS)
                && !Objects.equals(normalized, TYPE_NOTIFICATION)) {
            throw new BizException(400, "模板类型非法，仅支持EMAIL/SMS/NOTIFICATION");
        }
    }

    @MethodPurpose("按ID查询模板，不存在则抛出业务异常")
    private MessageTemplate requireTemplate(Long id) {
        if (id == null) {
            throw new BizException(400, "模板ID不能为空");
        }
        MessageTemplate template = messageTemplateMapper.selectById(id);
        if (template == null) {
            throw new BizException(404, "模板不存在");
        }
        return template;
    }

    @MethodPurpose("模板变量列表序列化为JSON")
    private String toVariablesJson(List<String> variables) {
        if (variables == null) {
            return null;
        }
        List<String> normalized = new ArrayList<>();
        for (String variable : variables) {
            if (StringUtils.hasText(variable)) {
                normalized.add(variable.trim());
            }
        }
        return JSONUtil.toJsonStr(normalized);
    }

    @MethodPurpose("将模板变量JSON反序列化为字符串列表")
    private List<String> parseVariables(String variablesJson) {
        if (!StringUtils.hasText(variablesJson)) {
            return new ArrayList<>();
        }
        try {
            return JSONUtil.toList(variablesJson, String.class);
        } catch (Exception e) {
            log.warn("解析模板变量失败, variables={}", variablesJson, e);
            return new ArrayList<>();
        }
    }

    @MethodPurpose("模板实体转换为详情视图对象")
    private MessageTemplateDetailVo toDetailVo(MessageTemplate template) {
        MessageTemplateDetailVo vo = new MessageTemplateDetailVo();
        vo.setId(template.getId());
        vo.setTemplateCode(template.getTemplateCode());
        vo.setTemplateName(template.getTemplateName());
        vo.setTemplateType(template.getTemplateType());
        vo.setTemplateSubject(template.getTemplateSubject());
        vo.setTemplateContent(template.getTemplateContent());
        vo.setVariables(parseVariables(template.getVariables()));
        vo.setDescription(template.getDescription());
        vo.setStatus(Boolean.TRUE.equals(template.getStatus()) ? 1 : 0);
        vo.setCreatedTime(template.getCreatedTime());
        vo.setUpdatedTime(template.getUpdatedTime());
        return vo;
    }

    @MethodPurpose("去除空白并转换为空值")
    private String trimToNull(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
    }

    @MethodPurpose("将整型状态值转换为布尔状态")
    private Boolean toStatusBoolean(Integer status, Boolean defaultValue) {
        if (status == null) {
            if (defaultValue == null) {
                throw new BizException(400, "模板状态不能为空");
            }
            return defaultValue;
        }
        if (status == 1) {
            return true;
        }
        if (status == 0) {
            return false;
        }
        throw new BizException(400, "模板状态只能为0或1");
    }
}
