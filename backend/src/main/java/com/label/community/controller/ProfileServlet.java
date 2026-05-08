package com.label.community.controller;

import com.label.community.dto.ProfileUpdateRequest;
import com.label.community.security.AuthUser;
import com.label.community.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/api/profile")
public class ProfileServlet extends BaseServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuthUser user = requiredUser();
        ok(response, userService.profile(user.getUserId()));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuthUser user = requiredUser();
        ProfileUpdateRequest updateRequest = readJson(request, ProfileUpdateRequest.class);
        ok(response, userService.updateProfile(user.getUserId(), updateRequest));
    }
}
