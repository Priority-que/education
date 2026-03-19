package com.xixi.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class SpaStaticResourceController {

    private static final List<String> DIST_LOCATIONS = List.of(
            "classpath:/static/",
            "file:../education_front/dist/",
            "file:../../education_front/dist/"
    );

    private final ResourceLoader resourceLoader;

    public SpaStaticResourceController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/assets/{*assetPath}")
    public ResponseEntity<Resource> getAsset(@PathVariable String assetPath) {
        String normalizedAssetPath = normalizeWildcardPath(assetPath);
        Resource resource = resolveResource("assets/" + normalizedAssetPath);
        if (resource == null) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        MediaType mediaType = MediaTypeFactorySafe.getMediaType(normalizedAssetPath);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .body(resource);
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Resource> getFavicon() {
        Resource resource = resolveResource("favicon.ico");
        if (resource == null) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/x-icon"))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                .body(resource);
    }

    private Resource resolveResource(String relativePath) {
        for (String location : DIST_LOCATIONS) {
            Resource resource = resourceLoader.getResource(location + relativePath);
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        }
        return null;
    }

    private String normalizeWildcardPath(String rawPath) {
        if (!StringUtils.hasText(rawPath)) {
            return "";
        }

        String normalized = rawPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        PathContainer container = PathContainer.parsePath(normalized);
        return container.value();
    }

    private static final class MediaTypeFactorySafe {
        private MediaTypeFactorySafe() {
        }

        private static MediaType getMediaType(String filename) {
            try {
                return org.springframework.http.MediaTypeFactory.getMediaType(filename)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM);
            } catch (Exception ignored) {
                return MediaType.APPLICATION_OCTET_STREAM;
            }
        }
    }
}
