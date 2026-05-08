package com.label.community.storage;

import java.io.IOException;
import java.io.InputStream;

public interface ImageStorageService {
    String save(InputStream inputStream, String originalName) throws IOException;
}
