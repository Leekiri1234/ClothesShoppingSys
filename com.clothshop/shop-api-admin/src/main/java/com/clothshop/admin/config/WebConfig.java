package com.clothshop.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private Path resolveWorkspaceRoot() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

        for (int i = 0; i < 6 && current != null; i++) {
            if (Files.exists(current.resolve("src/main/resources/static"))) {
                return current;
            }
            current = current.getParent();
        }

        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = resolveWorkspaceRoot().resolve("src/main/resources/static/uploads")
                .toAbsolutePath().toString().replace("\\", "/");


        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + uploadPath + "/");

        // Log để kiểm tra
        System.out.println("DEBUG: Admin đang lấy ảnh từ kho chung tại: " + uploadPath);
    }
}