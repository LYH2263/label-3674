package com.label.community.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet(urlPatterns = "/api/files/*")
public class FileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.sendError(404);
            return;
        }
        String fileName = pathInfo.substring(1);
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\\\")) {
            response.sendError(400);
            return;
        }

        String uploadDir = System.getenv().getOrDefault("APP_UPLOAD_DIR", "/tmp/smart-community-uploads");
        Path target = Path.of(uploadDir, fileName);
        if (!Files.exists(target)) {
            String objectDir = System.getenv().getOrDefault("APP_OBJECT_STORAGE_DIR", "/tmp/smart-community-objects");
            target = Path.of(objectDir, fileName);
        }
        if (!Files.exists(target)) {
            response.sendError(404);
            return;
        }

        String contentType = Files.probeContentType(target);
        response.setContentType(contentType == null ? "application/octet-stream" : contentType);
        Files.copy(target, response.getOutputStream());
    }
}
