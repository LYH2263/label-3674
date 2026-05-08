package com.label.community.controller;

import com.label.community.security.AuthUser;
import com.label.community.service.MessageService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/api/messages/*")
public class MessageServlet extends BaseServlet {
    private final MessageService messageService = new MessageService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuthUser user = requiredUser();
        ok(response, messageService.list(user.getUserId()));
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuthUser user = requiredUser();
        String[] segments = splitPath(request.getPathInfo());
        if (segments.length == 2 && "read".equals(segments[1])) {
            Long messageId = Long.valueOf(segments[0]);
            messageService.markRead(user.getUserId(), messageId);
            okMessage(response, "已标记为已读");
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
}
