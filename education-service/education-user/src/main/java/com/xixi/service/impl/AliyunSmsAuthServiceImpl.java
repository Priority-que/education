package com.xixi.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.xixi.config.AliyunSmsAuthProperties;
import com.xixi.exception.BizException;
import com.xixi.service.SmsAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AliyunSmsAuthServiceImpl implements SmsAuthService {
    private static final String PRODUCT = "Dypnsapi";
    private static final String VERSION = "2017-05-25";
    private static final String ACTION_SEND = "SendSmsVerifyCode";
    private static final String ACTION_CHECK = "CheckSmsVerifyCode";
    private static final String SUCCESS_CODE = "OK";
    private static final String VERIFY_PASS = "PASS";
    private static final String INVALID_PARAMETERS = "isv.INVALID_PARAMETERS";
    private static final String FREQUENCY_CODE = "biz.FREQUENCY";
    private static final String CODE_PLACEHOLDER = "##code##";

    private final AliyunSmsAuthProperties properties;

    @Override
    public String sendVerifyCode(String phone) {
        validateCommonConfig();
        if (!StringUtils.hasText(properties.getSignName())
                || !StringUtils.hasText(properties.getTemplateCode())
                || !StringUtils.hasText(properties.getTemplateParam())) {
            throw new BizException(500, "sms auth config missing: signName/templateCode/templateParam");
        }

        String currentTemplateParam = normalizeTemplateParam(properties.getTemplateParam());
        boolean hasCodePlaceholder = currentTemplateParam.contains(CODE_PLACEHOLDER);
        boolean useCodeType = hasCodePlaceholder;
        if (hasCodePlaceholder && !Boolean.TRUE.equals(properties.getUseCodeType())) {
            log.warn("sms auth config use-code-type=false but template contains ##code##, force useCodeType=true");
        }
        if (!hasCodePlaceholder && Boolean.TRUE.equals(properties.getUseCodeType())) {
            // Optional static-code mode: when template doesn't use ##code##, keep compatibility with old config.
            currentTemplateParam = injectStaticCodeWhenMissing(currentTemplateParam);
            useCodeType = false;
        }
        boolean includePolicyParams = true;

        JSONObject response = sendWithTemplateParam(phone, currentTemplateParam, useCodeType, includePolicyParams);
        String code = response.getStr("Code");
        if (!SUCCESS_CODE.equalsIgnoreCase(code) && useCodeType && INVALID_PARAMETERS.equalsIgnoreCase(code)) {
            throw new BizException(400,
                    "sms auth send failed: [isv.INVALID_PARAMETERS] "
                            + "please verify scheme-name/sign-name/template-code/template-param match Aliyun Dypns scheme config");
        }
        if (!SUCCESS_CODE.equalsIgnoreCase(code) && isFrequencyCode(code) && hasFrequencyPolicyConfig()) {
            log.warn("sms auth send got frequency control, retry without policy params, phone={}", maskPhone(phone));
            includePolicyParams = false;
            response = sendWithTemplateParam(phone, currentTemplateParam, useCodeType, includePolicyParams);
            code = response.getStr("Code");
        }
        if (!SUCCESS_CODE.equalsIgnoreCase(code)) {
            String message = response.getStr("Message");
            throw new BizException(400, "sms auth send failed: [" + safe(code) + "] " + safe(message));
        }

        JSONObject model = response.getJSONObject("Model");
        String requestId = model == null ? null : model.getStr("RequestId");
        if (!StringUtils.hasText(requestId)) {
            requestId = response.getStr("RequestId");
        }
        return requestId;
    }

    @Override
    public void checkVerifyCode(String phone, String smsCode) {
        validateCommonConfig();
        JSONObject response = request(
                ACTION_CHECK,
                request -> {
                    request.putQueryParameter("PhoneNumber", phone);
                    request.putQueryParameter("CountryCode", normalizeCountryCode(properties.getCountryCode()));
                    request.putQueryParameter("SchemeName", resolveSchemeName());
                    request.putQueryParameter("VerifyCode", smsCode);
                }
        );
        String code = response.getStr("Code");
        if (!SUCCESS_CODE.equalsIgnoreCase(code)) {
            String message = response.getStr("Message");
            throw new BizException(401, "sms auth verify failed: [" + safe(code) + "] " + safe(message));
        }

        JSONObject model = response.getJSONObject("Model");
        String verifyResult = model == null ? null : model.getStr("VerifyResult");
        if (!VERIFY_PASS.equalsIgnoreCase(verifyResult)) {
            throw new BizException(401, "sms auth verify failed");
        }
    }

    private JSONObject request(String action, RpcRequestCustomizer customizer) {
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysProtocol(ProtocolType.HTTPS);
        request.setSysDomain(properties.getEndpoint().trim());
        request.setSysProduct(PRODUCT);
        request.setSysVersion(VERSION);
        request.setSysAction(action);
        request.setSysRegionId(properties.getRegionId().trim());
        request.setSysConnectTimeout(properties.getConnectTimeoutMs());
        request.setSysReadTimeout(properties.getReadTimeoutMs());
        customizer.customize(request);

        try {
            log.info("aliyun sms auth request action={}, domain={}, region={}, query={}",
                    action,
                    request.getSysDomain(),
                    request.getSysRegionId(),
                    sanitizeQueryParameters(request.getSysQueryParameters()));
            CommonResponse response = buildClient().getCommonResponse(request);
            if (response.getHttpStatus() >= 400) {
                throw new BizException(500, "sms auth request failed with http status " + response.getHttpStatus());
            }
            JSONObject parsed = JSONUtil.parseObj(response.getData());
            log.info("aliyun sms auth response action={}, code={}, message={}, requestId={}",
                    action,
                    safe(parsed.getStr("Code")),
                    safe(parsed.getStr("Message")),
                    safe(resolveRequestId(parsed)));
            return parsed;
        } catch (BizException e) {
            throw e;
        } catch (ClientException e) {
            log.error("aliyun sms auth request failed, action={}", action, e);
            throw new BizException(500, "sms auth request failed");
        } catch (Exception e) {
            log.error("parse aliyun sms auth response failed, action={}", action, e);
            throw new BizException(500, "sms auth response parse failed");
        }
    }

    private IAcsClient buildClient() {
        DefaultProfile profile = DefaultProfile.getProfile(
                properties.getRegionId().trim(),
                properties.getAccessKeyId().trim(),
                properties.getAccessKeySecret().trim()
        );
        return new DefaultAcsClient(profile);
    }

    private void validateCommonConfig() {
        if (!StringUtils.hasText(properties.getAccessKeyId()) || !StringUtils.hasText(properties.getAccessKeySecret())) {
            throw new BizException(500, "sms auth config missing: accessKeyId/accessKeySecret");
        }
        if (!StringUtils.hasText(properties.getRegionId()) || !StringUtils.hasText(properties.getEndpoint())) {
            throw new BizException(500, "sms auth config missing: regionId/endpoint");
        }
    }

    private String safe(String message) {
        return StringUtils.hasText(message) ? message : "unknown";
    }

    private int resolveCodeType() {
        Integer codeType = properties.getCodeType();
        return codeType == null || codeType <= 0 ? 1 : codeType;
    }

    private int resolveCodeLength() {
        Integer codeLength = properties.getCodeLength();
        if (codeLength == null || codeLength < 4 || codeLength > 8) {
            return 6;
        }
        return codeLength;
    }

    private String normalizeCountryCode(String countryCode) {
        if (!StringUtils.hasText(countryCode)) {
            return "86";
        }
        return countryCode.trim();
    }

    private JSONObject sendWithTemplateParam(String phone, String templateParam, boolean useCodeType, boolean includePolicyParams) {
        log.info("send sms auth verify code, phone={}, useCodeType={}, includePolicyParams={}, interval={}, validTime={}, duplicatePolicy={}",
                maskPhone(phone),
                useCodeType,
                includePolicyParams,
                properties.getInterval(),
                properties.getValidTime(),
                properties.getDuplicatePolicy());
        log.info("sms auth config snapshot, schemeName={}, signName={}, templateCode={}, countryCode={}, templateParam={}",
                resolveSchemeName(),
                safe(properties.getSignName()),
                safe(properties.getTemplateCode()),
                normalizeCountryCode(properties.getCountryCode()),
                templateParam);
        return request(
                ACTION_SEND,
                request -> {
                    request.putQueryParameter("PhoneNumber", phone);
                    request.putQueryParameter("CountryCode", normalizeCountryCode(properties.getCountryCode()));
                    request.putQueryParameter("SchemeName", resolveSchemeName());
                    request.putQueryParameter("SignName", properties.getSignName().trim());
                    request.putQueryParameter("TemplateCode", properties.getTemplateCode().trim());
                    request.putQueryParameter("TemplateParam", templateParam);
                    if (includePolicyParams) {
                        if (properties.getInterval() != null && properties.getInterval() > 0) {
                            request.putQueryParameter("Interval", String.valueOf(properties.getInterval()));
                        }
                        if (properties.getValidTime() != null && properties.getValidTime() > 0) {
                            request.putQueryParameter("ValidTime", String.valueOf(properties.getValidTime()));
                        }
                        if (properties.getDuplicatePolicy() != null && properties.getDuplicatePolicy() >= 0) {
                            request.putQueryParameter("DuplicatePolicy", String.valueOf(properties.getDuplicatePolicy()));
                        }
                    }
                    if (properties.getReturnVerifyCode() != null) {
                        request.putQueryParameter("ReturnVerifyCode", String.valueOf(properties.getReturnVerifyCode()));
                    }
                    if (useCodeType) {
                        request.putQueryParameter("CodeType", String.valueOf(resolveCodeType()));
                        request.putQueryParameter("CodeLength", String.valueOf(resolveCodeLength()));
                    }
                }
        );
    }

    private String normalizeTemplateParam(String templateParam) {
        if (!StringUtils.hasText(templateParam)) {
            throw new BizException(500, "sms auth config missing: templateParam");
        }
        String normalized = templateParam.trim();
        try {
            // canonical JSON string path
            JSONObject obj = JSONUtil.parseObj(normalized);
            return obj.toString();
        } catch (Exception ignored) {
        }

        // nacos/yaml may bind "{code=##code##, min=5}" for unquoted map syntax
        if (normalized.startsWith("{") && normalized.endsWith("}") && normalized.contains("=")) {
            String inner = normalized.substring(1, normalized.length() - 1).trim();
            Map<String, Object> map = new LinkedHashMap<>();
            if (StringUtils.hasText(inner)) {
                String[] pairs = inner.split(",");
                for (String pair : pairs) {
                    String[] kv = pair.split("=", 2);
                    if (kv.length == 2 && StringUtils.hasText(kv[0])) {
                        map.put(kv[0].trim(), kv[1].trim());
                    }
                }
            }
            if (!map.isEmpty()) {
                return JSONUtil.parseObj(map).toString();
            }
        }

        throw new BizException(500, "invalid templateParam config, must be JSON string");
    }

    private String resolveSchemeName() {
        if (StringUtils.hasText(properties.getSchemeName())) {
            return properties.getSchemeName().trim();
        }
        return "default";
    }

    private String injectStaticCodeWhenMissing(String templateParam) {
        try {
            JSONObject obj = JSONUtil.parseObj(templateParam);
            if (obj.containsKey("code")) {
                Object value = obj.get("code");
                if (value == null || !StringUtils.hasText(String.valueOf(value)) || CODE_PLACEHOLDER.equals(String.valueOf(value))) {
                    obj.set("code", RandomUtil.randomNumbers(resolveCodeLength()));
                }
                return obj.toString();
            }
            return templateParam;
        } catch (Exception ignored) {
            return templateParam;
        }
    }

    private boolean isFrequencyCode(String code) {
        return StringUtils.hasText(code) && FREQUENCY_CODE.equalsIgnoreCase(code);
    }

    private boolean hasFrequencyPolicyConfig() {
        return (properties.getInterval() != null && properties.getInterval() > 0)
                || (properties.getValidTime() != null && properties.getValidTime() > 0)
                || (properties.getDuplicatePolicy() != null && properties.getDuplicatePolicy() >= 0);
    }

    private String resolveRequestId(JSONObject response) {
        JSONObject model = response.getJSONObject("Model");
        String requestId = model == null ? null : model.getStr("RequestId");
        if (StringUtils.hasText(requestId)) {
            return requestId;
        }
        return response.getStr("RequestId");
    }

    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return "unknown";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private Map<String, String> sanitizeQueryParameters(Map<String, String> query) {
        Map<String, String> result = new LinkedHashMap<>();
        if (query == null || query.isEmpty()) {
            return result;
        }
        for (Map.Entry<String, String> entry : query.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if ("PhoneNumber".equalsIgnoreCase(key)) {
                result.put(key, maskPhone(value));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    @FunctionalInterface
    private interface RpcRequestCustomizer {
        void customize(CommonRequest request);
    }
}
