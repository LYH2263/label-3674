package com.label.community.controller;

import com.label.community.dto.RepairCreateRequest;
import com.label.community.dto.RepairRatingRequest;
import com.label.community.dto.RepairStatusUpdateRequest;
import com.label.community.security.AuthUser;
import com.label.community.service.RepairService;
import com.label.community.storage.ImageStorageFactory;
import com.label.community.storage.ImageStorageService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@WebServlet(urlPatterns = "/api/repairs/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
public class RepairServlet extends BaseServlet {
    private final RepairService repairService = new RepairService();
    private final ImageStorageService imageStorageService = ImageStorageFactory.resolve();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuthUser user = requiredUser();
        String path = request.getPathInfo();
        if ("/upload-image".equals(path)) {
            var imagePart = request.getPart("image");
            if (imagePart == null || imagePart.getSize() == 0) {
                throw new com.label.community.common.BusinessException(400, 40046, "请选择要上传的图片");
            }
            String imageUrl = imageStorageService.save(imagePart.getInputStream(), imagePart.getSubmittedFileName());
            ok(response, java.util.Map.of("imageUrl", imageUrl));
            return;
        }

        if (path == null || "/".equals(path)) {
            String title;
            String description;
            String imageUrl = null;

            if (request.getContentType() != null && request.getContentType().startsWith("multipart/")) {
                title = partValue(request, "title");
                description = partValue(request, "description");
                var imagePart = request.getPart("image");
                if (imagePart != null && imagePart.getSize() > 0) {
                    imageUrl = imageStorageService.save(imagePart.getInputStream(), imagePart.getSubmittedFileName());
                }
            } else {
                RepairCreateRequest body = readJson(request, RepairCreateRequest.class);
                title = body.getTitle();
                description = body.getDescription();
                imageUrl = body.getImageUrl();
            }

            Long repairId = repairService.createRepair(user.getUserId(), title, description, imageUrl);
            ok(response, java.util.Map.of("repairId", repairId));
            return;
        }

        response.sendError(404);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        requiredUser();
        String path = request.getPathInfo();
        if ("/my".equals(path)) {
            ok(response, repairService.myRepairs(requiredUser().getUserId()));
            return;
        }
        if ("/board".equals(path)) {
            ok(response, repairService.providerBoard());
            return;
        }
        response.sendError(404);
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuthUser user = requiredUser();
        String[] parts = splitPath(request.getPathInfo());
        if (parts.length == 2 && "status".equals(parts[1])) {
            Long repairId = Long.valueOf(parts[0]);
            RepairStatusUpdateRequest body = readJson(request, RepairStatusUpdateRequest.class);
            repairService.updateStatus(user, repairId, body);
            okMessage(response, "状态更新成功");
            return;
        }
        if (parts.length == 2 && "rating".equals(parts[1])) {
            Long repairId = Long.valueOf(parts[0]);
            RepairRatingRequest body = readJson(request, RepairRatingRequest.class);
            repairService.rateRepair(user.getUserId(), repairId, body);
            okMessage(response, "评价提交成功");
            return;
        }
        response.sendError(404);
    }

    private String[] splitPath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return new String[0];
        }
        String trimmed = path.startsWith("/") ? path.substring(1) : path;
        return trimmed.split("/");
    }

    private String partValue(HttpServletRequest request, String field) throws IOException, ServletException {
        var part = request.getPart(field);
        if (part == null || part.getSize() == 0) {
            return null;
        }
        try (InputStream inputStream = part.getInputStream()) {
            return new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

}
