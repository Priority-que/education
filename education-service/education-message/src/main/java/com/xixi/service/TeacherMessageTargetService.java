package com.xixi.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xixi.constant.RoleConstants;
import com.xixi.exception.BizException;
import com.xixi.mapper.MessageRecipientMapper;
import com.xixi.pojo.vo.message.TeacherReceiverSearchVo;
import com.xixi.pojo.vo.message.TeacherTargetPreviewSampleVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeacherMessageTargetService {
    private static final String TARGET_ALL = "ALL";
    private static final String TARGET_ROLE = "ROLE";
    private static final String TARGET_COURSE = "COURSE";
    private static final String TARGET_CLASS = "CLASS";
    private static final String TARGET_USER_PICKED = "USER_PICKED";
    private static final String TARGET_USER_LEGACY = "USER";

    private static final LocalDate LEGACY_WARNING_DEADLINE = LocalDate.of(2026, 3, 31);
    private static final LocalDate LEGACY_DISABLE_DATE = LocalDate.of(2026, 4, 30);

    private final MessageRecipientMapper messageRecipientMapper;

    public ResolvedTeacherTarget resolveFromRequest(
            Long teacherId,
            String targetType,
            Map<String, Object> targetSpec,
            List<Object> legacyTargetValue
    ) {
        String normalizedTargetType = normalizeTargetType(targetType);
        if (TARGET_USER_LEGACY.equals(normalizedTargetType)) {
            return resolveLegacyRequest(teacherId, legacyTargetValue);
        }
        if (!isSupportedTargetType(normalizedTargetType)) {
            throw targetTypeInvalid();
        }

        Map<String, Object> safeSpec = targetSpec == null ? new LinkedHashMap<>() : targetSpec;
        TargetSpec normalizedSpec = normalizeTargetSpec(safeSpec);
        validateTargetSpec(normalizedTargetType, normalizedSpec, safeSpec.isEmpty());
        validateScope(teacherId, normalizedSpec);

        List<Long> recipientUserIds = resolveRecipients(teacherId, normalizedTargetType, normalizedSpec);
        return new ResolvedTeacherTarget(
                normalizedTargetType,
                toTargetSpecMap(normalizedTargetType, normalizedSpec),
                toTargetSpecJson(normalizedTargetType, normalizedSpec),
                recipientUserIds,
                null
        );
    }

    public ResolvedTeacherTarget resolveFromStored(
            Long teacherId,
            String targetType,
            String targetSpecJson
    ) {
        String normalizedTargetType = normalizeTargetType(targetType);
        if (TARGET_USER_LEGACY.equals(normalizedTargetType)) {
            return resolveLegacyStored(teacherId, targetSpecJson);
        }
        if (!isSupportedTargetType(normalizedTargetType)) {
            throw targetTypeInvalid();
        }

        Map<String, Object> specMap = parseTargetSpecJson(normalizedTargetType, targetSpecJson);
        TargetSpec normalizedSpec = normalizeTargetSpec(specMap);
        validateTargetSpec(normalizedTargetType, normalizedSpec, specMap.isEmpty());
        validateScope(teacherId, normalizedSpec);

        List<Long> recipientUserIds = resolveRecipients(teacherId, normalizedTargetType, normalizedSpec);
        return new ResolvedTeacherTarget(
                normalizedTargetType,
                toTargetSpecMap(normalizedTargetType, normalizedSpec),
                toTargetSpecJson(normalizedTargetType, normalizedSpec),
                recipientUserIds,
                null
        );
    }

    public List<TeacherTargetPreviewSampleVo> loadPreviewSamples(List<Long> recipientUserIds, int limit) {
        if (recipientUserIds == null || recipientUserIds.isEmpty() || limit <= 0) {
            return new ArrayList<>();
        }
        int end = Math.min(limit, recipientUserIds.size());
        List<Long> sampledUserIds = recipientUserIds.subList(0, end);

        List<Map<String, Object>> rows = messageRecipientMapper.selectReceiverSamplesByUserIds(sampledUserIds);
        Map<Long, TeacherTargetPreviewSampleVo> rowMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Long userId = toLong(row.get("userId"));
            if (userId == null) {
                continue;
            }
            TeacherTargetPreviewSampleVo sample = new TeacherTargetPreviewSampleVo();
            sample.setUserId(userId);
            sample.setName(toText(row.get("displayName")));
            sample.setStudentNo(toText(row.get("studentNo")));
            rowMap.put(userId, sample);
        }

        List<TeacherTargetPreviewSampleVo> result = new ArrayList<>();
        for (Long userId : sampledUserIds) {
            TeacherTargetPreviewSampleVo sample = rowMap.get(userId);
            if (sample != null) {
                result.add(sample);
            }
        }
        return result;
    }

    public ReceiverSearchPage searchReceivers(
            Long teacherId,
            String keyword,
            Long courseId,
            Long classId,
            int pageNum,
            int pageSize
    ) {
        int safePageNum = pageNum <= 0 ? 1 : pageNum;
        int safePageSize = pageSize <= 0 ? 20 : Math.min(pageSize, 100);
        long total = messageRecipientMapper.countTeacherReceivers(teacherId, keyword, courseId, classId);
        if (total <= 0) {
            return new ReceiverSearchPage(safePageNum, safePageSize, 0, new ArrayList<>());
        }

        long offset = (long) (safePageNum - 1) * safePageSize;
        List<Map<String, Object>> rows = messageRecipientMapper.searchTeacherReceivers(
                teacherId, keyword, courseId, classId, offset, safePageSize);
        List<TeacherReceiverSearchVo> list = rows.stream().map(this::toReceiverSearchVo).toList();
        return new ReceiverSearchPage(safePageNum, safePageSize, total, list);
    }

    private TeacherReceiverSearchVo toReceiverSearchVo(Map<String, Object> row) {
        TeacherReceiverSearchVo vo = new TeacherReceiverSearchVo();
        vo.setUserId(toLong(row.get("userId")));
        vo.setDisplayName(toText(row.get("displayName")));

        String studentNo = toText(row.get("studentNo"));
        String courseNames = toText(row.get("courseNames"));
        StringBuilder subTitle = new StringBuilder();
        if (StringUtils.hasText(studentNo)) {
            subTitle.append("学号:").append(studentNo.trim());
        }
        if (StringUtils.hasText(courseNames)) {
            if (!subTitle.isEmpty()) {
                subTitle.append(" / ");
            }
            subTitle.append(courseNames.trim());
        }
        vo.setSubTitle(subTitle.isEmpty() ? "可选接收人" : subTitle.toString());
        vo.setSelectable(Boolean.TRUE);
        return vo;
    }

    private ResolvedTeacherTarget resolveLegacyRequest(Long teacherId, List<Object> legacyTargetValue) {
        ensureLegacyAllowed();
        List<Long> userIds = normalizeLongList(legacyTargetValue);
        if (userIds.isEmpty()) {
            throw targetSpecInvalid("USER_PICKED目标必须包含至少一个userIds");
        }
        TargetSpec targetSpec = new TargetSpec();
        targetSpec.setUserIds(userIds);
        validateScope(teacherId, targetSpec);

        List<Long> recipientUserIds = resolveRecipients(teacherId, TARGET_USER_PICKED, targetSpec);
        return new ResolvedTeacherTarget(
                TARGET_USER_PICKED,
                toTargetSpecMap(TARGET_USER_PICKED, targetSpec),
                toTargetSpecJson(TARGET_USER_PICKED, targetSpec),
                recipientUserIds,
                legacyWarning()
        );
    }

    private ResolvedTeacherTarget resolveLegacyStored(Long teacherId, String legacyTargetValueJson) {
        List<Object> legacyTargetValue = parseJsonArray(legacyTargetValueJson);
        TargetSpec targetSpec = new TargetSpec();
        targetSpec.setUserIds(normalizeLongList(legacyTargetValue));
        if (targetSpec.getUserIds().isEmpty()) {
            throw targetSpecInvalid("USER_PICKED目标必须包含至少一个userIds");
        }
        validateScope(teacherId, targetSpec);
        List<Long> recipientUserIds = resolveRecipients(teacherId, TARGET_USER_PICKED, targetSpec);
        return new ResolvedTeacherTarget(
                TARGET_USER_PICKED,
                toTargetSpecMap(TARGET_USER_PICKED, targetSpec),
                toTargetSpecJson(TARGET_USER_PICKED, targetSpec),
                recipientUserIds,
                null
        );
    }

    private void validateScope(Long teacherId, TargetSpec targetSpec) {
        if (teacherId == null || teacherId <= 0) {
            throw new BizException(401, "未登录或用户身份缺失");
        }

        Set<Long> managedCourseIds = new LinkedHashSet<>(messageRecipientMapper.selectManagedCourseIdsByTeacherId(teacherId));
        if (!targetSpec.getCourseIds().isEmpty() && !managedCourseIds.containsAll(targetSpec.getCourseIds())) {
            throw scopeForbidden();
        }
        if (!targetSpec.getClassIds().isEmpty() && !managedCourseIds.containsAll(targetSpec.getClassIds())) {
            throw scopeForbidden();
        }
        if (!targetSpec.getUserIds().isEmpty()) {
            List<Long> managed = messageRecipientMapper.selectManagedUserIds(
                    teacherId, null, null, targetSpec.getUserIds());
            if (new LinkedHashSet<>(managed).size() != targetSpec.getUserIds().size()) {
                throw scopeForbidden();
            }
        }
    }

    private List<Long> resolveRecipients(Long teacherId, String targetType, TargetSpec targetSpec) {
        return switch (targetType) {
            case TARGET_ALL -> distinct(messageRecipientMapper.selectManagedUserIds(teacherId, null, null, null));
            case TARGET_COURSE -> distinct(messageRecipientMapper.selectManagedUserIds(
                    teacherId, targetSpec.getCourseIds(), null, null));
            case TARGET_CLASS -> distinct(messageRecipientMapper.selectManagedUserIds(
                    teacherId, targetSpec.getClassIds(), null, null));
            case TARGET_USER_PICKED -> distinct(messageRecipientMapper.selectManagedUserIds(
                    teacherId, null, null, targetSpec.getUserIds()));
            case TARGET_ROLE -> {
                List<Long> narrowedCourseIds = intersectCourseScopes(targetSpec.getCourseIds(), targetSpec.getClassIds());
                if (narrowedCourseIds != null && narrowedCourseIds.isEmpty()) {
                    yield new ArrayList<>();
                }
                yield distinct(messageRecipientMapper.selectManagedUserIds(
                        teacherId, narrowedCourseIds, targetSpec.getRoleCodes(), null));
            }
            default -> throw targetTypeInvalid();
        };
    }

    private List<Long> intersectCourseScopes(List<Long> courseIds, List<Long> classIds) {
        boolean hasCourse = courseIds != null && !courseIds.isEmpty();
        boolean hasClass = classIds != null && !classIds.isEmpty();
        if (!hasCourse && !hasClass) {
            return null;
        }
        if (!hasCourse) {
            return classIds;
        }
        if (!hasClass) {
            return courseIds;
        }
        Set<Long> intersection = new LinkedHashSet<>(courseIds);
        intersection.retainAll(new LinkedHashSet<>(classIds));
        return new ArrayList<>(intersection);
    }

    private Map<String, Object> parseTargetSpecJson(String targetType, String targetSpecJson) {
        if (!StringUtils.hasText(targetSpecJson)) {
            return new LinkedHashMap<>();
        }
        try {
            String json = targetSpecJson.trim();
            if (TARGET_USER_PICKED.equals(targetType) && json.startsWith("[")) {
                return new LinkedHashMap<>(Map.of("userIds", JSONUtil.parseArray(json).toList(Object.class)));
            }
            JSONObject jsonObject = JSONUtil.parseObj(json);
            return jsonObject;
        } catch (Exception e) {
            throw targetSpecInvalid("targetSpec格式错误");
        }
    }

    private TargetSpec normalizeTargetSpec(Map<String, Object> source) {
        TargetSpec targetSpec = new TargetSpec();
        targetSpec.setRoleCodes(normalizeRoleCodes(source.get("roleCodes")));
        targetSpec.setCourseIds(normalizeLongList(source.get("courseIds")));
        targetSpec.setClassIds(normalizeLongList(source.get("classIds")));
        targetSpec.setUserIds(normalizeLongList(source.get("userIds")));
        return targetSpec;
    }

    private void validateTargetSpec(String targetType, TargetSpec targetSpec, boolean rawSpecEmpty) {
        if (TARGET_ALL.equals(targetType)) {
            if (!targetSpec.getRoleCodes().isEmpty()
                    || !targetSpec.getCourseIds().isEmpty()
                    || !targetSpec.getClassIds().isEmpty()
                    || !targetSpec.getUserIds().isEmpty()) {
                throw targetSpecInvalid("ALL目标不需要targetSpec");
            }
            return;
        }
        if (rawSpecEmpty) {
            throw targetSpecRequired();
        }
        switch (targetType) {
            case TARGET_ROLE -> {
                if (targetSpec.getRoleCodes().isEmpty()) {
                    throw targetSpecInvalid("ROLE目标至少提供一个roleCodes");
                }
            }
            case TARGET_COURSE -> {
                if (targetSpec.getCourseIds().isEmpty()) {
                    throw targetSpecInvalid("COURSE目标至少提供一个courseIds");
                }
            }
            case TARGET_CLASS -> {
                if (targetSpec.getClassIds().isEmpty()) {
                    throw targetSpecInvalid("CLASS目标至少提供一个classIds");
                }
            }
            case TARGET_USER_PICKED -> {
                if (targetSpec.getUserIds().isEmpty()) {
                    throw targetSpecInvalid("USER_PICKED目标至少提供一个userIds");
                }
            }
            default -> throw targetTypeInvalid();
        }
    }

    private Map<String, Object> toTargetSpecMap(String targetType, TargetSpec targetSpec) {
        if (TARGET_ALL.equals(targetType)) {
            return new LinkedHashMap<>();
        }
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        if (!targetSpec.getRoleCodes().isEmpty()) {
            List<String> roleCodes = targetSpec.getRoleCodes().stream()
                    .map(this::toRoleCodeText)
                    .filter(StringUtils::hasText)
                    .toList();
            if (!roleCodes.isEmpty()) {
                map.put("roleCodes", roleCodes);
            }
        }
        if (!targetSpec.getCourseIds().isEmpty()) {
            map.put("courseIds", targetSpec.getCourseIds());
        }
        if (!targetSpec.getClassIds().isEmpty()) {
            map.put("classIds", targetSpec.getClassIds());
        }
        if (!targetSpec.getUserIds().isEmpty()) {
            map.put("userIds", targetSpec.getUserIds());
        }
        return map;
    }

    private String toTargetSpecJson(String targetType, TargetSpec targetSpec) {
        if (TARGET_ALL.equals(targetType)) {
            return null;
        }
        return JSONUtil.toJsonStr(toTargetSpecMap(targetType, targetSpec));
    }

    private List<Integer> normalizeRoleCodes(Object raw) {
        List<Object> list = toObjectList(raw);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        LinkedHashSet<Integer> codes = new LinkedHashSet<>();
        for (Object item : list) {
            Integer role = toRoleCode(item);
            if (role != null) {
                codes.add(role);
            }
        }
        return new ArrayList<>(codes);
    }

    private List<Long> normalizeLongList(Object raw) {
        List<Object> list = toObjectList(raw);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        LinkedHashSet<Long> values = new LinkedHashSet<>();
        for (Object item : list) {
            Long value = toLong(item);
            if (value != null && value > 0) {
                values.add(value);
            }
        }
        return new ArrayList<>(values);
    }

    private List<Object> toObjectList(Object raw) {
        if (raw == null) {
            return new ArrayList<>();
        }
        if (raw instanceof List<?> list) {
            return new ArrayList<>(list);
        }
        if (raw instanceof JSONArray array) {
            return array.toList(Object.class);
        }
        if (raw instanceof String text && StringUtils.hasText(text) && text.trim().startsWith("[")) {
            return parseJsonArray(text);
        }
        return new ArrayList<>(List.of(raw));
    }

    private List<Object> parseJsonArray(String json) {
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        try {
            return JSONUtil.parseArray(json).toList(Object.class);
        } catch (Exception e) {
            throw targetSpecInvalid("targetSpec格式错误");
        }
    }

    private Integer toRoleCode(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number number) {
            int role = number.intValue();
            return isRoleCodeSupported(role) ? role : null;
        }
        String text = String.valueOf(raw).trim();
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            int role = Integer.parseInt(text);
            return isRoleCodeSupported(role) ? role : null;
        } catch (NumberFormatException ignore) {
        }
        return switch (text.toUpperCase()) {
            case "ADMIN" -> RoleConstants.ADMIN;
            case "STUDENT" -> RoleConstants.STUDENT;
            case "TEACHER" -> RoleConstants.TEACHER;
            case "ENTERPRISE" -> RoleConstants.ENTERPRISE;
            default -> null;
        };
    }

    private String toRoleCodeText(Integer roleCode) {
        if (roleCode == null) {
            return null;
        }
        return switch (roleCode) {
            case RoleConstants.ADMIN -> "ADMIN";
            case RoleConstants.STUDENT -> "STUDENT";
            case RoleConstants.TEACHER -> "TEACHER";
            case RoleConstants.ENTERPRISE -> "ENTERPRISE";
            default -> null;
        };
    }

    private boolean isRoleCodeSupported(int roleCode) {
        return roleCode == RoleConstants.ADMIN
                || roleCode == RoleConstants.STUDENT
                || roleCode == RoleConstants.TEACHER
                || roleCode == RoleConstants.ENTERPRISE;
    }

    private String normalizeTargetType(String targetType) {
        if (!StringUtils.hasText(targetType)) {
            throw targetTypeInvalid();
        }
        return targetType.trim().toUpperCase();
    }

    private boolean isSupportedTargetType(String targetType) {
        return TARGET_ALL.equals(targetType)
                || TARGET_ROLE.equals(targetType)
                || TARGET_COURSE.equals(targetType)
                || TARGET_CLASS.equals(targetType)
                || TARGET_USER_PICKED.equals(targetType);
    }

    private void ensureLegacyAllowed() {
        LocalDate today = LocalDate.now();
        if (today.isAfter(LEGACY_DISABLE_DATE)) {
            throw new BizException(400, "MSG_LEGACY_PAYLOAD_UNSUPPORTED");
        }
    }

    private String legacyWarning() {
        LocalDate today = LocalDate.now();
        if (!today.isAfter(LEGACY_WARNING_DEADLINE)) {
            return "legacy targetValue payload is deprecated and will be removed after 2026-04-30";
        }
        return null;
    }

    private BizException targetTypeInvalid() {
        return new BizException(400, "MSG_TARGET_TYPE_INVALID");
    }

    private BizException targetSpecRequired() {
        return new BizException(400, "MSG_TARGET_SPEC_REQUIRED");
    }

    private BizException targetSpecInvalid(String detail) {
        return new BizException(400, "MSG_TARGET_SPEC_INVALID: " + detail);
    }

    private BizException scopeForbidden() {
        return new BizException(403, "MSG_TARGET_SCOPE_FORBIDDEN");
    }

    private <T> List<T> distinct(List<T> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        return source.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String toText(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return StringUtils.hasText(text) ? text : null;
    }

    public record ResolvedTeacherTarget(
            String targetType,
            Map<String, Object> targetSpec,
            String targetSpecJson,
            List<Long> recipientUserIds,
            String warning
    ) {
    }

    public record ReceiverSearchPage(
            int pageNum,
            int pageSize,
            long total,
            List<TeacherReceiverSearchVo> list
    ) {
    }

    private static class TargetSpec {
        private List<Integer> roleCodes = new ArrayList<>();
        private List<Long> courseIds = new ArrayList<>();
        private List<Long> classIds = new ArrayList<>();
        private List<Long> userIds = new ArrayList<>();

        public List<Integer> getRoleCodes() {
            return roleCodes;
        }

        public void setRoleCodes(List<Integer> roleCodes) {
            this.roleCodes = roleCodes == null ? new ArrayList<>() : roleCodes;
        }

        public List<Long> getCourseIds() {
            return courseIds;
        }

        public void setCourseIds(List<Long> courseIds) {
            this.courseIds = courseIds == null ? new ArrayList<>() : courseIds;
        }

        public List<Long> getClassIds() {
            return classIds;
        }

        public void setClassIds(List<Long> classIds) {
            this.classIds = classIds == null ? new ArrayList<>() : classIds;
        }

        public List<Long> getUserIds() {
            return userIds;
        }

        public void setUserIds(List<Long> userIds) {
            this.userIds = userIds == null ? new ArrayList<>() : userIds;
        }
    }
}
