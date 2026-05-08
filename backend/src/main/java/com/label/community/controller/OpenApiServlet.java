package com.label.community.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@WebServlet(urlPatterns = "/api/openapi.json")
public class OpenApiServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("openapi.json")) {
            if (stream == null) {
                resp.sendError(404);
                return;
            }
            resp.getOutputStream().write(stream.readAllBytes());
        }
    }
}
