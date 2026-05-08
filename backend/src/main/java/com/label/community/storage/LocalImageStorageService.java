package com.label.community.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class LocalImageStorageService implements ImageStorageService {
    @Override
    public String save(InputStream inputStream, String originalName) throws IOException {
        String uploadDir = System.getenv().getOrDefault("APP_UPLOAD_DIR", "/tmp/smart-community-uploads");
        Files.createDirectories(Path.of(uploadDir));
        String ext = resolveExt(originalName);
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = Path.of(uploadDir, fileName);
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        return "/api/files/" + fileName;
    }

    private String resolveExt(String originalName) {
        String ext = ".jpg";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
            if (ext.length() > 8) {
                ext = ".jpg";
            }
        }
        return ext;
    }
}
