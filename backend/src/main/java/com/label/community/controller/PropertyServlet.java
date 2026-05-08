package com.label.community.controller;

import com.label.community.dto.PaymentConfirmRequest;
import com.label.community.dto.PaymentIntentRequest;
import com.label.community.dto.VisitorCreateRequest;
import com.label.community.security.AuthUser;
import com.label.community.service.PropertyService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/api/property/*")
public class PropertyServlet extends BaseServlet {
    private final PropertyService propertyService = new PropertyService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuthUser user = requiredUser();
        String path = request.getPathInfo();
        if (path == null || "/overview".equals(path) || "/".equals(path)) {
            ok(response, propertyService.overview(user.getUserId()));
            return;
        }
        response.sendError(404);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuthUser user = requiredUser();
        String path = request.getPathInfo();
        if ("/visitors".equals(path)) {
            VisitorCreateRequest body = readJson(request, VisitorCreateRequest.class);
            propertyService.createVisitor(user.getUserId(), body);
            okMessage(response, "访客登记成功");
            return;
        }

        String[] segments = splitPath(path);
        if (segments.length == 3 && "payments".equals(segments[0]) && "pay-intent".equals(segments[2])) {
            Long paymentId = Long.valueOf(segments[1]);
            PaymentIntentRequest body = readJson(request, PaymentIntentRequest.class);
            ok(response, propertyService.createPayIntent(user.getUserId(), paymentId, body == null ? null : body.getChannel()));
            return;
        }
        if (segments.length == 3 && "payments".equals(segments[0]) && "confirm".equals(segments[2])) {
            Long paymentId = Long.valueOf(segments[1]);
            PaymentConfirmRequest body = readJson(request, PaymentConfirmRequest.class);
            propertyService.confirmPay(user.getUserId(), paymentId, body == null ? null : body.getPayNo());
            okMessage(response, "在线缴纳成功");
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
