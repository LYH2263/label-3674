package com.label.community.controller;

import com.label.community.config.RoleConstants;
import com.label.community.dao.UserDao;
import com.label.community.security.AuthUser;
import com.label.community.service.NotificationService;
import com.label.community.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = "/api/system/*")
public class SystemServlet extends BaseServlet {
    private final UserService userService = new UserService();
    private final NotificationService notificationService = new NotificationService();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuthUser user = requiredUser();
        String path = request.getPathInfo();
        if ("/overview".equals(path)) {
            ok(response, userService.personalCenter(user.getUserId()));
            return;
        }
        if ("/providers".equals(path)) {
            List<Map<String, Object>> providers = userDao.listByRole(RoleConstants.SERVICE_PROVIDER).stream().map(item -> {
                Map<String, Object> provider = new HashMap<>();
                provider.put("id", item.getId());
                provider.put("fullName", item.getFullName());
                provider.put("username", item.getUsername());
                return provider;
            }).collect(Collectors.toList());
            ok(response, providers);
            return;
        }
        if ("/intelligent-notices".equals(path)) {
            ok(response, notificationService.intelligentNotices(user.getUserId()));
            return;
        }
        response.sendError(404);
    }
}
