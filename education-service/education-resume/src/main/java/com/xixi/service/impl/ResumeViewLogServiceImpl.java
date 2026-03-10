package com.xixi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xixi.annotation.MethodPurpose;
import com.xixi.constant.RoleConstants;
import com.xixi.entity.Resume;
import com.xixi.entity.ResumeViewLog;
import com.xixi.exception.BizException;
import com.xixi.mapper.ResumeMapper;
import com.xixi.mapper.ResumeViewLogMapper;
import com.xixi.mq.ResumeViewLogChangedEventProducer;
import com.xixi.pojo.dto.resume.ResumeViewLogRecordDto;
import com.xixi.pojo.vo.resume.ResumeViewLogStatVo;
import com.xixi.pojo.vo.resume.ResumeViewLogVo;
import com.xixi.service.ResumeViewLogService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 简历浏览日志服务实现（9.1~9.3）。
 */
@Service
@RequiredArgsConstructor
public class ResumeViewLogServiceImpl implements ResumeViewLogService {
    private static final String VIEWER_ENTERPRISE = "ENTERPRISE";
    private static final String VIEWER_ADMIN = "ADMIN";
    private static final String VIEWER_STUDENT = "STUDENT";
    private static final String VIEWER_SYSTEM = "SYSTEM";

    private final ResumeMapper resumeMapper;
    private final ResumeViewLogMapper resumeViewLogMapper;
    private final ResumeViewLogChangedEventProducer resumeViewLogChangedEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @MethodPurpose("9.1：记录简历浏览行为，落库后在事务提交后投递浏览日志事件")
    public Result recordViewLog(
            ResumeViewLogRecordDto dto,
            Long operatorId,
            Integer operatorRole,
            String requestIp,
            String requestUserAgent
    ) {
        validateRecordDto(dto);
        Resume resume = requireResume(dto.getResumeId());

        Long viewerId = resolveViewerId(dto.getViewerId(), operatorId);
        String viewerType = resolveViewerType(dto.getViewerType(), operatorRole);
        LocalDateTime viewTime = dto.getViewTime() == null ? LocalDateTime.now() : dto.getViewTime();

        ResumeViewLog viewLog = new ResumeViewLog();
        viewLog.setResumeId(resume.getId());
        viewLog.setViewerId(viewerId);
        viewLog.setViewerType(viewerType);
        viewLog.setViewTime(viewTime);
        viewLog.setIpAddress(resolveText(dto.getIpAddress(), requestIp));
        viewLog.setUserAgent(resolveText(dto.getUserAgent(), requestUserAgent));
        viewLog.setCreatedTime(LocalDateTime.now());
        resumeViewLogMapper.insert(viewLog);

        resumeViewLogChangedEventProducer.publishRecord(
                viewLog.getId(),
                viewLog.getResumeId(),
                viewLog.getViewerId(),
                viewLog.getViewerType(),
                viewLog.getViewTime()
        );
        return Result.success("记录浏览行为成功", Map.of("id", viewLog.getId()));
    }

    @Override
    @MethodPurpose("9.2：分页查询当前学生指定简历的浏览日志")
    public IPage<ResumeViewLogVo> getMyViewLogPage(Long resumeId, Long studentId, Long pageNum, Long pageSize) {
        Long validStudentId = requireStudentId(studentId);
        requireOwnedResume(resumeId, validStudentId);

        long safePageNum = pageNum == null || pageNum <= 0 ? 1 : pageNum;
        long safePageSize = pageSize == null || pageSize <= 0 ? 10 : pageSize;

        Page<ResumeViewLog> entityPage = resumeViewLogMapper.selectPage(
                new Page<>(safePageNum, safePageSize),
                new LambdaQueryWrapper<ResumeViewLog>()
                        .eq(ResumeViewLog::getResumeId, resumeId)
                        .orderByDesc(ResumeViewLog::getViewTime, ResumeViewLog::getId)
        );

        Page<ResumeViewLogVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVo).toList());
        return voPage;
    }

    @Override
    @MethodPurpose("9.3：查询当前学生指定简历的浏览统计信息")
    public ResumeViewLogStatVo getMyViewStat(Long resumeId, Long studentId) {
        Long validStudentId = requireStudentId(studentId);
        requireOwnedResume(resumeId, validStudentId);

        Long totalCount = resumeViewLogMapper.selectCount(new LambdaQueryWrapper<ResumeViewLog>()
                .eq(ResumeViewLog::getResumeId, resumeId));

        ResumeViewLog latest = resumeViewLogMapper.selectOne(new LambdaQueryWrapper<ResumeViewLog>()
                .eq(ResumeViewLog::getResumeId, resumeId)
                .orderByDesc(ResumeViewLog::getViewTime, ResumeViewLog::getId)
                .last("limit 1"));

        Map<String, Long> distribution = initDistribution();
        List<Map<String, Object>> rows = resumeViewLogMapper.countViewerTypeDistribution(resumeId);
        for (Map<String, Object> row : rows) {
            Object viewerTypeObj = getMapValueIgnoreCase(row, "viewerType");
            Object totalCountObj = getMapValueIgnoreCase(row, "totalCount");
            String viewerType = viewerTypeObj == null ? null : String.valueOf(viewerTypeObj).toUpperCase();
            Long count = parseLong(totalCountObj);
            if (viewerType != null && count != null) {
                distribution.put(viewerType, count);
            }
        }

        ResumeViewLogStatVo statVo = new ResumeViewLogStatVo();
        statVo.setTotalViewCount(totalCount == null ? 0L : totalCount);
        statVo.setLastViewTime(latest == null ? null : latest.getViewTime());
        statVo.setViewerTypeDistribution(distribution);
        return statVo;
    }

    @MethodPurpose("校验记录浏览参数")
    private void validateRecordDto(ResumeViewLogRecordDto dto) {
        if (dto == null) {
            throw new BizException(400, "请求参数不能为空");
        }
        if (dto.getResumeId() == null || dto.getResumeId() <= 0) {
            throw new BizException(400, "简历ID不能为空");
        }
    }

    @MethodPurpose("校验并返回当前学生ID")
    private Long requireStudentId(Long studentId) {
        if (studentId == null || studentId <= 0) {
            throw new BizException(401, "未登录或用户ID缺失");
        }
        return studentId;
    }

    @MethodPurpose("按简历ID查询简历")
    private Resume requireResume(Long resumeId) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new BizException(404, "简历不存在");
        }
        return resume;
    }

    @MethodPurpose("按简历ID查询并校验归属")
    private Resume requireOwnedResume(Long resumeId, Long studentId) {
        Resume resume = requireResume(resumeId);
        if (!Objects.equals(resume.getStudentId(), studentId)) {
            throw new BizException(403, "无权限查看他人简历浏览日志");
        }
        return resume;
    }

    @MethodPurpose("优先使用网关透传的操作人ID，其次使用请求体中的viewerId")
    private Long resolveViewerId(Long requestViewerId, Long operatorId) {
        if (operatorId != null && operatorId > 0) {
            return operatorId;
        }
        if (requestViewerId != null && requestViewerId > 0) {
            return requestViewerId;
        }
        return null;
    }

    @MethodPurpose("根据角色码或请求参数解析浏览者类型")
    private String resolveViewerType(String requestViewerType, Integer operatorRole) {
        String fromRole = mapViewerTypeByRole(operatorRole);
        if (StringUtils.hasText(fromRole)) {
            return fromRole;
        }
        if (!StringUtils.hasText(requestViewerType)) {
            return VIEWER_SYSTEM;
        }
        String normalized = requestViewerType.trim().toUpperCase();
        if (!VIEWER_ENTERPRISE.equals(normalized)
                && !VIEWER_ADMIN.equals(normalized)
                && !VIEWER_STUDENT.equals(normalized)
                && !VIEWER_SYSTEM.equals(normalized)) {
            throw new BizException(400, "viewerType仅支持ENTERPRISE/ADMIN/STUDENT/SYSTEM");
        }
        return normalized;
    }

    @MethodPurpose("通过角色码映射浏览者类型")
    private String mapViewerTypeByRole(Integer role) {
        if (role == null) {
            return null;
        }
        if (role == RoleConstants.ENTERPRISE) {
            return VIEWER_ENTERPRISE;
        }
        if (role == RoleConstants.ADMIN) {
            return VIEWER_ADMIN;
        }
        if (role == RoleConstants.STUDENT) {
            return VIEWER_STUDENT;
        }
        return null;
    }

    @MethodPurpose("优先取请求体文本，未传时回退到请求上下文文本")
    private String resolveText(String fromBody, String fromRequest) {
        if (StringUtils.hasText(fromBody)) {
            return fromBody.trim();
        }
        if (StringUtils.hasText(fromRequest)) {
            return fromRequest.trim();
        }
        return null;
    }

    @MethodPurpose("初始化浏览者类型分布结构")
    private Map<String, Long> initDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put(VIEWER_ENTERPRISE, 0L);
        distribution.put(VIEWER_ADMIN, 0L);
        distribution.put(VIEWER_STUDENT, 0L);
        distribution.put(VIEWER_SYSTEM, 0L);
        return distribution;
    }

    @MethodPurpose("安全转换统计列为Long")
    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @MethodPurpose("忽略大小写读取Map中的字段值")
    private Object getMapValueIgnoreCase(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        if (map.containsKey(key)) {
            return map.get(key);
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @MethodPurpose("实体转浏览日志视图对象")
    private ResumeViewLogVo toVo(ResumeViewLog entity) {
        return BeanUtil.copyProperties(entity, ResumeViewLogVo.class);
    }
}
