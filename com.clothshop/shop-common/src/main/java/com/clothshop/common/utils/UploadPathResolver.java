package com.clothshop.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class UploadPathResolver {

    @Value("${app.upload-root:}")
    private String configuredUploadRoot;

    public Path resolveUploadRoot() {
        if (StringUtils.hasText(configuredUploadRoot)) {
            return Paths.get(configuredUploadRoot).toAbsolutePath().normalize();
        }
        return resolveWorkspaceFallback().resolve("src/main/resources/static/uploads").toAbsolutePath().normalize();
    }

    private Path resolveWorkspaceFallback() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        for (int i = 0; i < 6 && current != null; i++) {
            if (Files.exists(current.resolve("src/main/resources/static"))) {
                return current;
            }
            current = current.getParent();
        }
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }
}
