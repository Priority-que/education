package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.pojo.dto.EnterpriseVerificationApplyDto;
import com.xixi.pojo.dto.EnterpriseVerificationAuditDto;
import com.xixi.pojo.query.EnterpriseVerificationHistoryQuery;
import com.xixi.pojo.vo.EnterpriseVerificationVo;
import com.xixi.service.EnterpriseVerificationService;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enterprise/verification")
@RequiredArgsConstructor
public class EnterpriseVerificationController {
    private final EnterpriseVerificationService enterpriseVerificationService;

    @PostMapping("/apply")
    public Result apply(
            @RequestBody(required = false) EnterpriseVerificationApplyDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return enterpriseVerificationService.apply(dto, parseLong(userIdHeader));
    }

    @GetMapping("/current")
    public Result current(
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        EnterpriseVerificationVo vo = enterpriseVerificationService.current(parseLong(userIdHeader));
        return Result.success(vo);
    }

    @GetMapping("/history/page")
    public Result historyPage(
            EnterpriseVerificationHistoryQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String userRoleHeader
    ) {
        IPage<EnterpriseVerificationVo> page = enterpriseVerificationService.historyPage(
                query,
                parseLong(userIdHeader),
                parseInteger(userRoleHeader)
        );
        return Result.success(page);
    }

    @GetMapping("/history/my/page")
    public Result historyMyPage(
            EnterpriseVerificationHistoryQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<EnterpriseVerificationVo> page = enterpriseVerificationService.historyMyPage(
                query,
                parseLong(userIdHeader)
        );
        return Result.success(page);
    }

    @GetMapping("/history/admin/page")
    public Result historyAdminPage(
            EnterpriseVerificationHistoryQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String userRoleHeader
    ) {
        IPage<EnterpriseVerificationVo> page = enterpriseVerificationService.historyAdminPage(
                query,
                parseLong(userIdHeader),
                parseInteger(userRoleHeader)
        );
        return Result.success(page);
    }

    @PostMapping("/audit/{applicationId}")
    public Result audit(
            @PathVariable Long applicationId,
            @RequestBody EnterpriseVerificationAuditDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ROLE, required = false) String userRoleHeader
    ) {
        return enterpriseVerificationService.audit(
                applicationId,
                dto,
                parseLong(userIdHeader),
                parseInteger(userRoleHeader)
        );
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
