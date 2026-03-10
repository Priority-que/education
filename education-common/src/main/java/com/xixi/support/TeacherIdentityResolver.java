package com.xixi.support;

import com.xixi.exception.BizException;
import com.xixi.openfeign.user.EducationUserTeacherClient;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TeacherIdentityResolver {

    private final EducationUserTeacherClient educationUserTeacherClient;

    public Long resolveTeacherIdByUserId(Long userId) {
        if (userId == null) {
            throw new BizException(401, "未登录或用户ID缺失");
        }

        Result result = educationUserTeacherClient.getTeacherIdByUserId(userId);
        Long teacherId = extractTeacherId(result == null ? null : result.getData());
        if (result == null || result.getCode() != 200 || teacherId == null || teacherId <= 0) {
            throw new BizException(404, "当前用户未绑定教师档案");
        }
        return teacherId;
    }

    @SuppressWarnings("unchecked")
    private Long extractTeacherId(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof Number number) {
            return number.longValue();
        }
        if (data instanceof Map<?, ?> rawMap) {
            Object teacherId = rawMap.get("teacherId");
            if (teacherId == null) {
                teacherId = rawMap.get("id");
            }
            return toLong(teacherId);
        }
        return toLong(data);
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
