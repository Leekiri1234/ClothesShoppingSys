package com.clothshop.client.config;

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
        // 1. Lấy đường dẫn gốc của Project (Root Workspace)
        Path rootPath = resolveWorkspaceRoot();

        // --- XỬ LÝ KHO UPLOADS (Ảnh do người dùng up lên) ---
        Path uploadPath = rootPath.resolve("src/main/resources/static/uploads").normalize();
        String uploadLocation = uploadPath.toUri().toString();
        if (!uploadLocation.endsWith("/")) uploadLocation += "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation)
                .setCachePeriod(0);

        // --- XỬ LÝ KHO IMAGES (Ảnh hệ thống, no-image.png, v.v.) ---
        Path imagesPath = rootPath.resolve("src/main/resources/static/images").normalize();

        // Kiểm tra xem folder images có tồn tại không để debug cho dễ
        if (!Files.exists(imagesPath)) {
            System.err.println("CẢNH BÁO: Không tìm thấy thư mục images tại: " + imagesPath);
        }

        String imagesLocation = imagesPath.toUri().toString();
        if (!imagesLocation.endsWith("/")) imagesLocation += "/";

        registry.addResourceHandler("/images/**")
                .addResourceLocations(imagesLocation)
                .setCachePeriod(0);

        // Log để kiểm tra trong Console khi khởi động
        System.out.println("DEBUG: Uploads Location: " + uploadLocation);
        System.out.println("DEBUG: Static Images Location: " + imagesLocation);
    }
}