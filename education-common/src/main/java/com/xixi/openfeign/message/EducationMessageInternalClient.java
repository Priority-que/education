package com.xixi.openfeign.message;

import com.xixi.openfeign.message.dto.TemplateTriggerEventRequest;
import com.xixi.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * education-message 内部消息投递远程调用。
 */
@FeignClient(name = "education-message", contextId = "educationMessageInternalClient")
public interface EducationMessageInternalClient {

    @PostMapping("/message/internal/send/user")
    Result sendToUser(
            @RequestHeader("X-User-Id") Long operatorId,
            @RequestHeader("X-User-Role") Integer operatorRole,
            @RequestBody Map<String, Object> payload
    );

    @PostMapping("/message/internal/trigger/event")
    Result triggerEvent(
            @RequestHeader("X-User-Id") Long operatorId,
            @RequestHeader("X-User-Role") Integer operatorRole,
            @RequestBody TemplateTriggerEventRequest payload
    );
}
