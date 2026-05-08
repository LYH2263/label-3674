package com.label.community.controller;

import com.label.community.common.ApiResponse;
import com.label.community.common.BusinessException;
import com.label.community.common.JsonUtil;
import com.label.community.common.RequestUtil;
import com.label.community.security.AuthContext;
import com.label.community.security.AuthUser;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
            return;
        }
        super.service(req, resp);
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    protected AuthUser requiredUser() {
        AuthUser user = AuthContext.get();
        if (user == null) {
            throw new BusinessException(401, 40100, "请先登录");
        }
        return user;
    }

    protected <T> T readJson(HttpServletRequest request, Class<T> type) {
        String body = RequestUtil.readBody(request);
        if (body == null || body.isBlank()) {
            throw new BusinessException(400, 40003, "请求体不能为空");
        }
        return JsonUtil.read(body, type);
    }

    protected void ok(HttpServletResponse response, Object data) throws IOException {
        JsonUtil.writeResponse(response, 200, ApiResponse.success(data));
    }

    protected void okMessage(HttpServletResponse response, String message) throws IOException {
        JsonUtil.writeResponse(response, 200, ApiResponse.successMessage(message, null));
    }
}
