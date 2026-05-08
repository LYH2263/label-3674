package com.label.community.storage;

public final class ImageStorageFactory {
    private static final ImageStorageService LOCAL = new LocalImageStorageService();
    private static final ImageStorageService OBJECT = new ObjectImageStorageService();

    private ImageStorageFactory() {
    }

    public static ImageStorageService resolve() {
        String storageType = System.getenv().getOrDefault("APP_STORAGE_TYPE", "LOCAL").trim().toUpperCase();
        if ("OBJECT".equals(storageType)) {
            return OBJECT;
        }
        return LOCAL;
    }
}
