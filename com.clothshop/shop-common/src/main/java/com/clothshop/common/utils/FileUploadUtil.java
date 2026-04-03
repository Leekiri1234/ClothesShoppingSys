package com.clothshop.common.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Component
public class FileUploadUtil {

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    public String upload(MultipartFile file, String folder) {
        try {
            // 📌 Tạo folder nếu chưa có
            Path uploadPath = Paths.get(UPLOAD_DIR + folder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 📌 Tạo tên file unique
            String originalFileName = file.getOriginalFilename();
            String extension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID().toString() + extension;

            // 📌 Lưu file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 📌 Trả về path lưu DB
            return folder + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Upload file thất bại", e);
        }
    }
}