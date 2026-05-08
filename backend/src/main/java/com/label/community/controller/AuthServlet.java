package com.label.community.controller;

import com.label.community.dto.LoginRequest;
import com.label.community.dto.RegisterRequest;
import com.label.community.security.AuthUser;
import com.label.community.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/api/auth/*")
public class AuthServlet extends BaseServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if ("/register".equals(path)) {
            RegisterRequest registerRequest = readJson(request, RegisterRequest.class);
            var payload = authService.register(registerRequest);
            request.getSession(true).setAttribute("LOGIN_USER_ID", ((java.util.Map<?, ?>) payload.get("user")).get("id"));
            ok(response, payload);
            return;
        }
        if ("/login".equals(path)) {
            LoginRequest loginRequest = readJson(request, LoginRequest.class);
            var payload = authService.login(loginRequest);
            request.getSession(true).setAttribute("LOGIN_USER_ID", ((java.util.Map<?, ?>) payload.get("user")).get("id"));
            ok(response, payload);
            return;
        }
        if ("/logout".equals(path)) {
            request.getSession().removeAttribute("LOGIN_USER_ID");
            okMessage(response, "退出成功");
            return;
        }
        response.sendError(404);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if ("/me".equals(path)) {
            AuthUser authUser = requiredUser();
            ok(response, authService.getProfileSnapshot(authUser.getUserId()));
            return;
        }
        response.sendError(404);
    }
}
