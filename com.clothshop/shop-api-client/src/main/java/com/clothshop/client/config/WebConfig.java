package com.clothshop.client.config;

import com.clothshop.common.utils.UploadPathResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UploadPathResolver uploadPathResolver;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String uploadPath = uploadPathResolver.resolveUploadRoot()
                .toAbsolutePath()
                .toString()
                .replace("\\", "/");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + uploadPath + "/");
    }
}