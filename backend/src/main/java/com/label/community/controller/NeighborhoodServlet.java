package com.label.community.controller;

import com.label.community.dto.NeighborhoodPostCreateRequest;
import com.label.community.security.AuthUser;
import com.label.community.service.NeighborhoodService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = "/api/neighborhood/*")
public class NeighborhoodServlet extends BaseServlet {
    private final NeighborhoodService neighborhoodService = new NeighborhoodService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        requiredUser();
        String path = request.getPathInfo();
        if (path == null || "/".equals(path) || "/posts".equals(path)) {
            String category = request.getParameter("category");
            int page = parseIntOrDefault(request.getParameter("page"), 1);
            int pageSize = parseIntOrDefault(request.getParameter("pageSize"), 8);
            ok(response, neighborhoodService.list(category, page, pageSize));
            return;
        }
        response.sendError(404);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuthUser user = requiredUser();
        String path = request.getPathInfo();
        if (path == null || "/".equals(path) || "/posts".equals(path)) {
            NeighborhoodPostCreateRequest body = readJson(request, NeighborhoodPostCreateRequest.class);
            Long postId = neighborhoodService.createPost(user.getUserId(), body);
            ok(response, Map.of("postId", postId));
            return;
        }
        response.sendError(404);
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuthUser user = requiredUser();
        String[] segments = splitPath(request.getPathInfo());
        if (segments.length == 2 && "close".equals(segments[1])) {
            Long postId = Long.valueOf(segments[0]);
            neighborhoodService.closePost(user.getUserId(), postId);
            okMessage(response, "帖子已关闭");
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

    private int parseIntOrDefault(String text, int defaultValue) {
        if (text == null || text.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
