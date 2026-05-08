package com.label.community.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class ObjectImageStorageService implements ImageStorageService {
    @Override
    public String save(InputStream inputStream, String originalName) throws IOException {
        String objectDir = System.getenv().getOrDefault("APP_OBJECT_STORAGE_DIR", "/tmp/smart-community-objects");
        String publicBase = System.getenv().getOrDefault("APP_OBJECT_BASE_URL", "/api/files");
        Files.createDirectories(Path.of(objectDir));

        String ext = resolveExt(originalName);
        String objectKey = "obj_" + UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = Path.of(objectDir, objectKey);
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        return publicBase + "/" + objectKey;
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
