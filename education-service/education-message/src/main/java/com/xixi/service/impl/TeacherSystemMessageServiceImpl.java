package com.xixi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.SystemMessage;
import com.xixi.exception.BizException;
import com.xixi.mapper.SystemMessageMapper;
import com.xixi.mq.SystemMessagePublishCommandProducer;
import com.xixi.pojo.dto.message.SystemMessageCreateDto;
import com.xixi.pojo.dto.message.TeacherTargetPreviewDto;
import com.xixi.pojo.query.message.TeacherSystemMessageHistoryQuery;
import com.xixi.pojo.vo.message.TeacherSystemMessageHistoryVo;
import com.xixi.service.TeacherMessageTargetService;
import com.xixi.service.TeacherSystemMessageService;
import com.xixi.support.TeacherIdentityResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TeacherSystemMessageServiceImpl implements TeacherSystemMessageService {
    private static final Set<String> ALLOWED_MESSAGE_TYPES = Set.of(
            "SYSTEM", "COURSE", "CERTIFICATE", "OTHER",
            "NOTICE", "REMINDER", "ANNOUNCEMENT"
    );
    private static final Set<String> ALLOWED_TARGET_TYPES = Set.of(
            "ALL", "ROLE", "COURSE", "CLASS", "USER_PICKED", "USER"
    );

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_WITHDRAWN = "WITHDRAWN";
    private static final int MAX_RECEIVER_COUNT = 50000;

    private final SystemMessageMapper systemMessageMapper;
    private final TeacherMessageTargetService teacherMessageTargetService;
    private final SystemMessagePublishCommandProducer publishCommandProducer;
    private final TeacherIdentityResolver teacherIdentityResolver;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("教师公告创建草稿")
    public Result createTeacherSystemMessage(SystemMessageCreateDto dto, Long operatorId, Integer operatorRole) {
        requireTeacher(operatorId, operatorRole);
        validateCreateDto(dto);
        Long teacherId = teacherIdentityResolver.resolveTeacherIdByUserId(operatorId);

        TeacherMessageTargetService.ResolvedTeacherTarget resolvedTarget = teacherMessageTargetService.resolveFromRequest(
                teacherId, dto.getTargetType(), dto.getTargetSpec(), dto.getTargetValue());
        ensureReceiverLimit(resolvedTarget.recipientUserIds().size());

        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setMessageType(dto.getMessageType().trim().toUpperCase());
        systemMessage.setMessageTitle(dto.getMessageTitle().trim());
        systemMessage.setMessageContent(dto.getMessageContent().trim());
        systemMessage.setSenderId(operatorId);
        systemMessage.setSenderName(StringUtils.hasText(dto.getSenderName()) ? dto.getSenderName().trim() : "教师");
        systemMessage.setPriority(dto.getPriority() == null ? 1 : dto.getPriority());
        systemMessage.setTargetType(resolvedTarget.targetType());
        systemMessage.setTargetValue(resolvedTarget.targetSpecJson());
        systemMessage.setExpiryTime(dto.getExpiryTime());
        systemMessage.setStatus(STATUS_DRAFT);

        LocalDateTime now = LocalDateTime.now();
        systemMessage.setCreatedTime(now);
        systemMessage.setUpdatedTime(now);
        systemMessageMapper.insert(systemMessage);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("messageId", systemMessage.getId());
        data.put("status", systemMessage.getStatus());
        data.put("targetType", resolvedTarget.targetType());
        data.put("targetSpec", resolvedTarget.targetSpec());
        data.put("receiverCountPreview", resolvedTarget.recipientUserIds().size());
        if (StringUtils.hasText(resolvedTarget.warning())) {
            data.put("warning", resolvedTarget.warning());
        }
        return Result.success("创建教师公告草稿成功", data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("教师公告发布")
    public Result publishTeacherSystemMessage(Long id, Long operatorId, Integer operatorRole) {
        requireTeacher(operatorId, operatorRole);
        Long teacherId = teacherIdentityResolver.resolveTeacherIdByUserId(operatorId);
        if (id == null) {
            throw new BizException(400, "MSG_DRAFT_NOT_FOUND");
        }

        SystemMessage systemMessage = systemMessageMapper.selectById(id);
        if (systemMessage == null) {
            throw new BizException(404, "MSG_DRAFT_NOT_FOUND");
        }
        if (!operatorId.equals(systemMessage.getSenderId())) {
            throw new BizException(403, "MSG_DRAFT_NOT_OWNER");
        }
        String status = systemMessage.getStatus();
        if (!STATUS_DRAFT.equals(status) && !STATUS_WITHDRAWN.equals(status)) {
            throw new BizException(409, "仅草稿或已撤回状态可发布");
        }

        TeacherMessageTargetService.ResolvedTeacherTarget resolvedTarget = teacherMessageTargetService.resolveFromStored(
                teacherId, systemMessage.getTargetType(), systemMessage.getTargetValue());
        int recipientCount = resolvedTarget.recipientUserIds().size();
        if (recipientCount <= 0) {
            throw new BizException(400, "MSG_TARGET_EMPTY");
        }
        ensureReceiverLimit(recipientCount);

        LocalDateTime publishTime = LocalDateTime.now();
        systemMessage.setTargetType(resolvedTarget.targetType());
        systemMessage.setTargetValue(resolvedTarget.targetSpecJson());
        systemMessage.setStatus(STATUS_PUBLISHED);
        systemMessage.setPublishTime(publishTime);
        systemMessage.setUpdatedTime(publishTime);
        systemMessageMapper.updateById(systemMessage);

        publishCommandProducer.publish(systemMessage.getId(), operatorId, recipientCount, publishTime);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("publishedCount", recipientCount);
        data.put("publishTime", publishTime);
        if (StringUtils.hasText(resolvedTarget.warning())) {
            data.put("warning", resolvedTarget.warning());
        }
        return Result.success("发布教师公告成功", data);
    }

    @Override
    @MethodPurpose("教师公告目标预览")
    public Result previewTeacherTarget(TeacherTargetPreviewDto dto, Long operatorId, Integer operatorRole) {
        requireTeacher(operatorId, operatorRole);
        Long teacherId = teacherIdentityResolver.resolveTeacherIdByUserId(operatorId);
        if (dto == null) {
            throw new BizException(400, "MSG_TARGET_SPEC_REQUIRED");
        }
        if (!StringUtils.hasText(dto.getTargetType())) {
            throw new BizException(400, "MSG_TARGET_TYPE_INVALID");
        }

        TeacherMessageTargetService.ResolvedTeacherTarget resolvedTarget = teacherMessageTargetService.resolveFromRequest(
                teacherId, dto.getTargetType(), dto.getTargetSpec(), dto.getTargetValue());
        ensureReceiverLimit(resolvedTarget.recipientUserIds().size());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("receiverCount", resolvedTarget.recipientUserIds().size());
        data.put("sampleUsers", teacherMessageTargetService.loadPreviewSamples(resolvedTarget.recipientUserIds(), 10));
        if (StringUtils.hasText(resolvedTarget.warning())) {
            data.put("warning", resolvedTarget.warning());
        }
        return Result.success(data);
    }

    @Override
    @MethodPurpose("教师可选接收人查询")
    public Result searchTeacherReceivers(
            Long operatorId,
            Integer operatorRole,
            String keyword,
            Long courseId,
            Long classId,
            Integer pageNum,
            Integer pageSize
    ) {
        requireTeacher(operatorId, operatorRole);
        Long teacherId = teacherIdentityResolver.resolveTeacherIdByUserId(operatorId);
        int safePageNum = pageNum == null || pageNum <= 0 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize <= 0 ? 20 : Math.min(pageSize, 100);
        TeacherMessageTargetService.ReceiverSearchPage page = teacherMessageTargetService.searchReceivers(
                teacherId, keyword, courseId, classId, safePageNum, safePageSize);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pageNum", page.pageNum());
        data.put("pageSize", page.pageSize());
        data.put("total", page.total());
        data.put("list", page.list());
        return Result.success(data);
    }

    @Override
    @MethodPurpose("教师公告历史分页")
    public Result getTeacherSystemMessageHistoryPage(
            TeacherSystemMessageHistoryQuery query,
            Long operatorId,
            Integer operatorRole
    ) {
        requireTeacher(operatorId, operatorRole);

        TeacherSystemMessageHistoryQuery safeQuery =
                query == null ? new TeacherSystemMessageHistoryQuery() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() <= 0 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() <= 0
                ? 20
                : Math.min(safeQuery.getPageSize(), 100);

        String normalizedStatus = null;
        if (StringUtils.hasText(safeQuery.getStatus())) {
            normalizedStatus = safeQuery.getStatus().trim().toUpperCase();
            if (!STATUS_DRAFT.equals(normalizedStatus)
                    && !STATUS_PUBLISHED.equals(normalizedStatus)
                    && !STATUS_WITHDRAWN.equals(normalizedStatus)) {
                throw new BizException(400, "MSG_HISTORY_STATUS_INVALID");
            }
        }

        LambdaQueryWrapper<SystemMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemMessage::getSenderId, operatorId);
        wrapper.in(SystemMessage::getStatus, STATUS_DRAFT, STATUS_PUBLISHED, STATUS_WITHDRAWN);
        if (normalizedStatus != null) {
            wrapper.eq(SystemMessage::getStatus, normalizedStatus);
        }
        if (StringUtils.hasText(safeQuery.getMessageType())) {
            wrapper.eq(SystemMessage::getMessageType, safeQuery.getMessageType().trim().toUpperCase());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(w -> w.like(SystemMessage::getMessageTitle, keyword)
                    .or()
                    .like(SystemMessage::getMessageContent, keyword));
        }
        wrapper.orderByDesc(SystemMessage::getUpdatedTime, SystemMessage::getCreatedTime, SystemMessage::getId);

        Page<SystemMessage> entityPage = systemMessageMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<TeacherSystemMessageHistoryVo> voPage =
                new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toHistoryVo).toList());
        return Result.success(voPage);
    }

    private void validateCreateDto(SystemMessageCreateDto dto) {
        if (dto == null) {
            throw new BizException(400, "创建参数不能为空");
        }
        if (!StringUtils.hasText(dto.getMessageType())) {
            throw new BizException(400, "messageType不能为空");
        }
        if (!StringUtils.hasText(dto.getMessageTitle())) {
            throw new BizException(400, "messageTitle不能为空");
        }
        if (!StringUtils.hasText(dto.getMessageContent())) {
            throw new BizException(400, "messageContent不能为空");
        }
        if (!StringUtils.hasText(dto.getTargetType())) {
            throw new BizException(400, "MSG_TARGET_TYPE_INVALID");
        }

        String messageType = dto.getMessageType().trim().toUpperCase();
        if (!ALLOWED_MESSAGE_TYPES.contains(messageType)) {
            throw new BizException(400, "messageType仅支持SYSTEM/COURSE/CERTIFICATE/OTHER/NOTICE/REMINDER/ANNOUNCEMENT");
        }

        String targetType = dto.getTargetType().trim().toUpperCase();
        if (!ALLOWED_TARGET_TYPES.contains(targetType)) {
            throw new BizException(400, "MSG_TARGET_TYPE_INVALID");
        }

        int titleLength = dto.getMessageTitle().trim().length();
        if (titleLength < 1 || titleLength > 100) {
            throw new BizException(400, "messageTitle长度需在1~100之间");
        }

        int contentLength = dto.getMessageContent().trim().length();
        if (contentLength < 1 || contentLength > 2000) {
            throw new BizException(400, "messageContent长度需在1~2000之间");
        }

        validatePriority(dto.getPriority());
    }

    private void validatePriority(Integer priority) {
        int safePriority = priority == null ? 1 : priority;
        if (safePriority < 1 || safePriority > 3) {
            throw new BizException(400, "priority仅支持1~3");
        }
    }

    private void ensureReceiverLimit(int receiverCount) {
        if (receiverCount > MAX_RECEIVER_COUNT) {
            throw new BizException(400, "MSG_TARGET_SCOPE_FORBIDDEN: 接收人数量超过上限50000");
        }
    }

    private void requireTeacher(Long operatorId, Integer operatorRole) {
        if (operatorId == null) {
            throw new BizException(401, "未登录或用户身份缺失");
        }
        if (!Objects.equals(operatorRole, RoleConstants.TEACHER)) {
            throw new BizException(403, "仅教师可操作");
        }
    }

    private TeacherSystemMessageHistoryVo toHistoryVo(SystemMessage entity) {
        TeacherSystemMessageHistoryVo vo = new TeacherSystemMessageHistoryVo();
        vo.setId(entity.getId());
        vo.setMessageType(entity.getMessageType());
        vo.setMessageTitle(entity.getMessageTitle());
        vo.setMessageContent(entity.getMessageContent());
        vo.setPriority(entity.getPriority());
        vo.setTargetType(entity.getTargetType());
        vo.setTargetSpecJson(entity.getTargetValue());
        vo.setStatus(entity.getStatus());
        vo.setCanPublish(STATUS_DRAFT.equals(entity.getStatus()) || STATUS_WITHDRAWN.equals(entity.getStatus()));
        vo.setPublishTime(entity.getPublishTime());
        vo.setExpiryTime(entity.getExpiryTime());
        vo.setCreatedTime(entity.getCreatedTime());
        vo.setUpdatedTime(entity.getUpdatedTime());
        return vo;
    }
}
