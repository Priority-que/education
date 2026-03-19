package com.xixi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "auth.whitelist")
public class AuthWhitelistProperties {
    private List<String> paths = new ArrayList<>(List.of(
            "/auth/login",
            "/auth/refresh",
            "/course/getCourseList",
            "/course/getCourseDetail/**"
    ));

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
}
