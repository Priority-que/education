package com.xixi.controller;

import com.xixi.utils.AliyunOSSUpload;
import com.xixi.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 文件上传接口。
 */
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private static final long IMAGE_MAX_SIZE = 10 * 1024 * 1024L;
    private static final long AVATAR_MAX_SIZE = 5 * 1024 * 1024L;
    private static final long VIDEO_MAX_SIZE = 500 * 1024 * 1024L;
    private static final long MATERIAL_MAX_SIZE = 100 * 1024 * 1024L;

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".bmp", ".gif", ".webp"
    );

    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            ".mp4", ".mov", ".avi", ".wmv", ".mkv", ".flv", ".webm", ".m4v"
    );

    private static final Set<String> MATERIAL_EXTENSIONS = Set.of(
            ".pdf", ".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx", ".txt", ".zip", ".rar", ".7z"
    );

    private static final Set<String> MATERIAL_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "application/zip",
            "application/x-zip-compressed",
            "application/x-rar-compressed",
            "application/vnd.rar"
    );

    private final AliyunOSSUpload aliyunOSSUpload;

    /**
     * 上传图片到 OSS 并返回访问地址。
     */
    @PostMapping("/image")
    public Result uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        return uploadWithRule(file, IMAGE_MAX_SIZE, this::isImageFile, "图片大小不能超过10MB", "仅支持图片格式", "image");
    }

    /**
     * 上传用户头像到 OSS 并返回访问地址。
     */
    @PostMapping("/avatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file) throws Exception {
        return uploadWithRule(file, AVATAR_MAX_SIZE, this::isImageFile, "头像大小不能超过5MB", "仅支持图片格式", "avatar");
    }

    /**
     * 上传视频文件到 OSS 并返回访问地址。
     */
    @PostMapping("/video")
    public Result uploadVideo(@RequestParam("file") MultipartFile file) throws Exception {
        return uploadWithRule(file, VIDEO_MAX_SIZE, this::isVideoFile, "视频大小不能超过500MB", "仅支持视频格式", "video");
    }

    /**
     * 上传课程资料文件到 OSS 并返回访问地址。
     */
    @PostMapping("/material")
    public Result uploadMaterial(@RequestParam("file") MultipartFile file) throws Exception {
        return uploadWithRule(file, MATERIAL_MAX_SIZE, this::isMaterialFile, "资料大小不能超过100MB", "仅支持文档资料格式", "material");
    }

    private Result uploadWithRule(MultipartFile file,
                                  long maxSize,
                                  Predicate<MultipartFile> fileValidator,
                                  String sizeErrorMessage,
                                  String typeErrorMessage,
                                  String bizDir) throws Exception {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        if (file.getSize() > maxSize) {
            return Result.error(sizeErrorMessage);
        }
        if (!fileValidator.test(file)) {
            return Result.error(typeErrorMessage);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            return Result.error("文件名不能为空");
        }

        String url = aliyunOSSUpload.upload(bizDir, fileName, file.getBytes());
        return Result.success(Collections.singletonMap("url", url));
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = normalizeContentType(file.getContentType());
        return contentType.startsWith("image/") || hasAllowedExtension(file, IMAGE_EXTENSIONS);
    }

    private boolean isVideoFile(MultipartFile file) {
        String contentType = normalizeContentType(file.getContentType());
        return contentType.startsWith("video/") || hasAllowedExtension(file, VIDEO_EXTENSIONS);
    }

    private boolean isMaterialFile(MultipartFile file) {
        String contentType = normalizeContentType(file.getContentType());
        return MATERIAL_CONTENT_TYPES.contains(contentType) || hasAllowedExtension(file, MATERIAL_EXTENSIONS);
    }

    private boolean hasAllowedExtension(MultipartFile file, Set<String> allowedExtensions) {
        String extension = getFileExtension(file.getOriginalFilename());
        return !extension.isEmpty() && allowedExtensions.contains(extension);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
    }
}
