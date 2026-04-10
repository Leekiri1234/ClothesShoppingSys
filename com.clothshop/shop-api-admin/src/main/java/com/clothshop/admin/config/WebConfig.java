package com.clothshop.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Lấy đường dẫn thư mục hiện tại khi chạy app
        String userDir = System.getProperty("user.dir");
        Path rootPath = Paths.get(userDir);

        // 2. Nếu IDE đang đứng ở module 'shop-api-admin', ta nhảy ra ngoài 1 cấp để về gốc Project
        if (userDir.endsWith("shop-api-admin")) {
            rootPath = rootPath.getParent();
        }

        String uploadPath = rootPath.resolve("src/main/resources/static/uploads")
                .toAbsolutePath().toString().replace("\\", "/");


        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + uploadPath + "/");

        // Log để kiểm tra
        System.out.println("DEBUG: Admin đang lấy ảnh từ kho chung tại: " + uploadPath);
    }
}