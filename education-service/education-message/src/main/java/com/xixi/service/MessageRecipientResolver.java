package com.xixi.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xixi.exception.BizException;
import com.xixi.mapper.MessageRecipientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MessageRecipientResolver {
    private static final String TARGET_ALL = "ALL";
    private static final String TARGET_ROLE = "ROLE";
    private static final String TARGET_USER = "USER";

    private final MessageRecipientMapper messageRecipientMapper;

    public List<Long> resolveUserIds(String targetType, String targetValueJson) {
        if (!StringUtils.hasText(targetType)) {
            throw new BizException(400, "targetType不能为空");
        }

        String normalizedTargetType = targetType.trim().toUpperCase();
        return switch (normalizedTargetType) {
            case TARGET_ALL -> distinct(messageRecipientMapper.selectAllEnabledUserIds());
            case TARGET_ROLE -> resolveByRoles(parseStoredTargetValue(normalizedTargetType, targetValueJson));
            case TARGET_USER -> resolveByUserIds(parseStoredTargetValue(normalizedTargetType, targetValueJson));
            default -> throw new BizException(400, "targetType非法，仅支持ALL/ROLE/USER");
        };
    }

    public int countRecipients(String targetType, String targetValueJson) {
        return resolveUserIds(targetType, targetValueJson).size();
    }

    public String normalizeTargetValue(String targetType, List<Object> targetValue) {
        if (!StringUtils.hasText(targetType)) {
            throw new BizException(400, "targetType不能为空");
        }
        String normalizedTargetType = targetType.trim().toUpperCase();
        if (TARGET_ALL.equals(normalizedTargetType)) {
            return null;
        }
        if (targetValue == null || targetValue.isEmpty()) {
            throw new BizException(400, "targetValue不能为空");
        }
        if (TARGET_USER.equals(normalizedTargetType)) {
            List<Long> userIds = normalizeUserIds(targetValue);
            if (userIds.isEmpty()) {
                throw new BizException(400, "USER目标必须提供有效用户ID");
            }
            return JSONUtil.toJsonStr(userIds);
        }
        if (TARGET_ROLE.equals(normalizedTargetType)) {
            List<Integer> roles = normalizeRoles(targetValue);
            if (roles.isEmpty()) {
                throw new BizException(400, "ROLE目标必须提供有效角色");
            }
            return JSONUtil.toJsonStr(roles);
        }
        throw new BizException(400, "targetType非法，仅支持ALL/ROLE/USER");
    }

    public List<Object> parseStoredTargetValue(String targetType, String targetValueJson) {
        String normalizedTargetType = StringUtils.hasText(targetType) ? targetType.trim().toUpperCase() : "";
        return parseTargetValueAsList(normalizedTargetType, targetValueJson);
    }

    private List<Long> resolveByRoles(List<Object> roleValues) {
        List<Integer> roles = normalizeRoles(roleValues);
        if (roles.isEmpty()) {
            throw new BizException(400, "ROLE目标必须提供有效角色");
        }
        return distinct(messageRecipientMapper.selectEnabledUserIdsByRoles(roles));
    }

    private List<Long> resolveByUserIds(List<Object> userValues) {
        List<Long> userIds = normalizeUserIds(userValues);
        if (userIds.isEmpty()) {
            throw new BizException(400, "USER目标必须提供有效用户ID");
        }
        return distinct(messageRecipientMapper.selectEnabledUserIdsByUserIds(userIds));
    }

    private List<Integer> normalizeRoles(List<Object> roleValues) {
        Set<Integer> roleSet = new LinkedHashSet<>();
        for (Object value : roleValues) {
            Integer role = toRoleCode(value);
            if (role != null) {
                roleSet.add(role);
            }
        }
        return new ArrayList<>(roleSet);
    }

    private List<Long> normalizeUserIds(List<Object> userValues) {
        Set<Long> userIdSet = new LinkedHashSet<>();
        for (Object value : userValues) {
            Long userId = toLong(value);
            if (userId != null && userId > 0) {
                userIdSet.add(userId);
            }
        }
        return new ArrayList<>(userIdSet);
    }

    private Integer toRoleCode(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            int role = number.intValue();
            return isValidRole(role) ? role : null;
        }
        String text = String.valueOf(value).trim();
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            int role = Integer.parseInt(text);
            return isValidRole(role) ? role : null;
        } catch (NumberFormatException ignore) {
        }
        return switch (text.toUpperCase()) {
            case "ADMIN" -> 1;
            case "STUDENT" -> 2;
            case "TEACHER" -> 3;
            case "ENTERPRISE" -> 4;
            default -> null;
        };
    }

    private boolean isValidRole(int role) {
        return role >= 1 && role <= 4;
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

    private List<Object> parseTargetValueAsList(String targetType, String targetValueJson) {
        if (!StringUtils.hasText(targetValueJson)) {
            return new ArrayList<>();
        }
        try {
            Object parsed = JSONUtil.parse(targetValueJson);
            if (parsed instanceof JSONArray jsonArray) {
                return jsonArray.toList(Object.class);
            }
            if (parsed instanceof JSONObject jsonObject) {
                return parseLegacyTargetObject(targetType, jsonObject);
            }
        } catch (Exception e) {
            throw new BizException(400, "targetValue格式非法");
        }
        throw new BizException(400, "targetValue格式非法");
    }

    private List<Object> parseLegacyTargetObject(String targetType, JSONObject jsonObject) {
        for (String fieldName : legacyFieldCandidates(targetType)) {
            if (!jsonObject.containsKey(fieldName)) {
                continue;
            }
            return castTargetFieldToList(jsonObject.get(fieldName));
        }
        if (jsonObject.size() == 1) {
            return castTargetFieldToList(jsonObject.values().iterator().next());
        }
        throw new BizException(400, "targetValue格式非法");
    }

    private List<String> legacyFieldCandidates(String targetType) {
        if (TARGET_ROLE.equals(targetType)) {
            return List.of("roleCodes", "roles", "targetValue", "values");
        }
        if (TARGET_USER.equals(targetType)) {
            return List.of("userIds", "users", "ids", "targetValue", "values");
        }
        return List.of("targetValue", "values", "roleCodes", "userIds", "ids");
    }

    private List<Object> castTargetFieldToList(Object fieldValue) {
        if (fieldValue == null) {
            return new ArrayList<>();
        }
        if (fieldValue instanceof JSONArray jsonArray) {
            return jsonArray.toList(Object.class);
        }
        if (fieldValue instanceof List<?> list) {
            return new ArrayList<>(list);
        }
        if (fieldValue instanceof Number || fieldValue instanceof CharSequence) {
            String text = String.valueOf(fieldValue).trim();
            if (!StringUtils.hasText(text)) {
                return new ArrayList<>();
            }
            if (text.startsWith("[") && text.endsWith("]")) {
                return JSONUtil.parseArray(text).toList(Object.class);
            }
            return new ArrayList<>(List.of(text));
        }
        throw new BizException(400, "targetValue格式非法");
    }

    private <T> List<T> distinct(List<T> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(new LinkedHashSet<>(list));
    }
}
