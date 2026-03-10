package com.xixi.support;

import cn.hutool.json.JSONUtil;
import com.xixi.exception.BizException;
import com.xixi.openfeign.user.EducationUserStudentClient;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 学生身份解析器：将 userId 转换为 studentId。
 */
@Component
@RequiredArgsConstructor
public class StudentIdentityResolver {

    private final EducationUserStudentClient educationUserStudentClient;

    public Long resolveStudentIdByUserId(Long userId) {
        if (userId == null) {
            throw new BizException(401, "未登录或用户ID缺失");
        }

        Result result = educationUserStudentClient.getStudentByUserId(userId);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BizException(404, "当前用户未绑定学生档案");
        }

        Long studentId = extractStudentId(result.getData());
        if (studentId == null) {
            throw new BizException(500, "解析学生ID失败");
        }
        return studentId;
    }

    @SuppressWarnings("unchecked")
    private Long extractStudentId(Object data) {
        if (data instanceof Map<?, ?> map) {
            return toLong(map.get("id"));
        }

        Map<String, Object> dataMap = JSONUtil.toBean(JSONUtil.toJsonStr(data), Map.class);
        return toLong(dataMap.get("id"));
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
