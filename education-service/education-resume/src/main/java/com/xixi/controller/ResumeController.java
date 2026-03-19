package com.xixi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xixi.annotation.MethodPurpose;
import com.xixi.annotation.RoleRequired;
import com.xixi.constant.AuthHeaderConstants;
import com.xixi.constant.RoleConstants;
import com.xixi.exception.BizException;
import com.xixi.pojo.dto.resume.ResumeCreateDto;
import com.xixi.pojo.dto.resume.ResumeUpdateDto;
import com.xixi.pojo.query.resume.ResumeQuery;
import com.xixi.pojo.vo.resume.ResumePdfExportResult;
import com.xixi.pojo.vo.resume.ResumeVo;
import com.xixi.service.ResumePdfExportService;
import com.xixi.service.ResumeService;
import com.xixi.support.StudentIdentityResolver;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简历主档接口（5.1~5.6）。
 */
@Slf4j
@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
@RoleRequired({RoleConstants.STUDENT})
public class ResumeController {
    private final ResumeService resumeService;
    private final ResumePdfExportService resumePdfExportService;
    private final StudentIdentityResolver studentIdentityResolver;

    @MethodPurpose("5.1：创建简历主档")
    @PostMapping("/create")
    public Result createResume(
            @RequestBody ResumeCreateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return resumeService.createResume(dto, resolveStudentId(userIdHeader));
    }

    @MethodPurpose("5.2：修改简历主档")
    @PutMapping("/update")
    public Result updateResume(
            @RequestBody ResumeUpdateDto dto,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return resumeService.updateResume(dto, resolveStudentId(userIdHeader));
    }

    @MethodPurpose("5.3：删除简历主档")
    @DeleteMapping("/delete/{resumeId}")
    public Result deleteResume(
            @PathVariable Long resumeId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return resumeService.deleteResume(resumeId, resolveStudentId(userIdHeader));
    }

    @MethodPurpose("5.4-扩展：查询当前学生指定简历详情")
    @GetMapping("/detail/{resumeId}")
    public Result getResumeDetail(
            @PathVariable Long resumeId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return Result.success(resumeService.getResumeDetail(resumeId, resolveStudentId(userIdHeader)));
    }

    @MethodPurpose("5.4：分页查询当前学生的简历")
    @GetMapping("/my/page")
    public Result getMyResumePage(
            ResumeQuery query,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        IPage<ResumeVo> page = resumeService.getMyResumePage(query, resolveStudentId(userIdHeader));
        return Result.success(page);
    }

    @MethodPurpose("5.4-扩展：导出当前学生指定简历 PDF")
    @GetMapping("/export/pdf/{resumeId}")
    public ResponseEntity<byte[]> exportResumePdf(
            @PathVariable Long resumeId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        try {
            ResumePdfExportResult result = resumePdfExportService.exportMyResumePdf(resumeId, resolveStudentId(userIdHeader));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodeFileName(result.getFileName()))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(result.getContent());
        } catch (BizException e) {
            log.warn("resume pdf export business error, resumeId={}, message={}", resumeId, e.getMessage());
            return buildPdfErrorResponse(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("resume pdf export unexpected error, resumeId={}", resumeId, e);
            return buildPdfErrorResponse(500, "简历 PDF 导出失败，请稍后重试");
        }
    }

    @MethodPurpose("5.5：设置默认简历")
    @PutMapping("/default/{resumeId}")
    public Result setDefaultResume(
            @PathVariable Long resumeId,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return resumeService.setDefaultResume(resumeId, resolveStudentId(userIdHeader));
    }

    @MethodPurpose("5.6：设置简历公开状态")
    @PutMapping("/visibility/{resumeId}")
    public Result setVisibility(
            @PathVariable Long resumeId,
            @RequestParam String visibility,
            @RequestHeader(value = AuthHeaderConstants.HEADER_USER_ID, required = false) String userIdHeader
    ) {
        return resumeService.setVisibility(resumeId, visibility, resolveStudentId(userIdHeader));
    }

    @MethodPurpose("将用户ID解析为学生ID")
    private Long resolveStudentId(String userIdHeader) {
        return studentIdentityResolver.resolveStudentIdByUserId(parseUserId(userIdHeader));
    }

    private String encodeFileName(String fileName) {
        return java.net.URLEncoder.encode(fileName, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }

    private ResponseEntity<byte[]> buildPdfErrorResponse(int statusCode, String message) {
        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null || !status.isError()) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType("text/plain;charset=UTF-8"))
                .body(message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @MethodPurpose("解析请求头中的用户ID")
    private Long parseUserId(String userIdHeader) {
        if (!StringUtils.hasText(userIdHeader)) {
            return null;
        }
        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
