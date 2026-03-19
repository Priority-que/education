package com.xixi.support;

import com.xixi.context.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentTeacherResolver {

    private final TeacherIdentityResolver teacherIdentityResolver;

    public Long requireCurrentTeacherId() {
        return teacherIdentityResolver.resolveTeacherIdByUserId(CurrentUserUtil.requireUserId());
    }
}
