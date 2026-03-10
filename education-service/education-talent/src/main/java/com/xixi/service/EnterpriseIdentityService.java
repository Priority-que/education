package com.xixi.service;

import cn.hutool.json.JSONUtil;
import com.xixi.annotation.MethodPurpose;
import com.xixi.exception.BizException;
import com.xixi.openfeign.user.EducationUserEnterpriseClient;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 企业身份解析服务。
 */
@Service
@RequiredArgsConstructor
public class EnterpriseIdentityService {
    private final EducationUserEnterpriseClient educationUserEnterpriseClient;

    @MethodPurpose("根据用户ID解析企业ID，不存在则抛出业务异常")
    public Long requireEnterpriseId(Long userId) {
        if (userId == null) {
            throw new BizException(401, "未登录或用户ID缺失");
        }
        Result result = educationUserEnterpriseClient.getEnterpriseByUserId(userId);
        if (result == null || result.getCode() == null || result.getCode() != 200 || result.getData() == null) {
            throw new BizException(404, "企业信息不存在");
        }
        if (result.getData() instanceof Map<?, ?> map) {
            Object id = map.get("id");
            if (id != null) {
                return Long.parseLong(String.valueOf(id));
            }
        }
        Object id = JSONUtil.parseObj(JSONUtil.toJsonStr(result.getData())).get("id");
        if (id == null) {
            throw new BizException(404, "企业ID解析失败");
        }
        return Long.parseLong(String.valueOf(id));
    }
}
