package com.clothshop.common.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Component
public class FileUploadUtil {

    private Path resolveUploadRoot() {
        Path rootPath = Paths.get(System.getProperty("user.dir"));
        String userDir = rootPath.toString();

        if (userDir.endsWith("shop-api-admin") || userDir.endsWith("shop-api-client")) {
            rootPath = rootPath.getParent();
        }

        return rootPath.resolve("src/main/resources/static/uploads").toAbsolutePath().normalize();
    }

    public String upload(MultipartFile file, String folder) {
        try {
            // 📌 Tạo folder nếu chưa có
            Path uploadPath = resolveUploadRoot().resolve(folder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 📌 Tạo tên file unique
            String originalFileName = file.getOriginalFilename();
            String extension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + extension;

            // 📌 Lưu file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 📌 Trả về đúng path web (cho browser truy cập)
            return "/uploads/" + folder + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Upload file thất bại", e);
        }
    }
}