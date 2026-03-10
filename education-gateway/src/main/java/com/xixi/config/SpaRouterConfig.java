package com.xixi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.List;

@Configuration
public class SpaRouterConfig {

    private static final List<String> API_PREFIXES = List.of(
            "/api",
            "/course",
            "/courseCategory",
            "/courseChapter",
            "/courseComment",
            "/courseDiscussion",
            "/courseFavorite",
            "/courseMaterial",
            "/courseVideo",
            "/upload",
            "/users",
            "/students",
            "/teacher",
            "/teachers",
            "/enterprise",
            "/enterprises",
            "/auth",
            "/study",
            "/admin",
            "/certificate",
            "/message",
            "/resume",
            "/talent"
    );

    @Bean
    public RouterFunction<ServerResponse> spaFallbackRoute(ResourceLoader resourceLoader) {
        Resource indexHtml = resolveIndexHtml(resourceLoader);

        RequestPredicate spaPredicate = RequestPredicates.GET("/{*path}")
                .and(this::isSpaRoute);

        return RouterFunctions.route(
                spaPredicate,
                request -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .bodyValue(indexHtml)
        );
    }

    private boolean isSpaRoute(ServerRequest request) {
        String path = request.path();
        if (path == null || path.isBlank() || "/".equals(path)) {
            return true;
        }

        // 静态资源通常包含扩展名，例如 .js/.css/.png
        if (path.contains(".")) {
            return false;
        }

        return API_PREFIXES.stream().noneMatch(prefix ->
                path.equals(prefix) || path.startsWith(prefix + "/")
        );
    }

    private Resource resolveIndexHtml(ResourceLoader resourceLoader) {
        List<String> locations = List.of(
                "classpath:/static/index.html",
                "file:../education_front/dist/index.html",
                "file:../../education_front/dist/index.html"
        );

        for (String location : locations) {
            Resource resource = resourceLoader.getResource(location);
            if (resource.exists()) {
                return resource;
            }
        }

        return resourceLoader.getResource("classpath:/static/index.html");
    }
}
