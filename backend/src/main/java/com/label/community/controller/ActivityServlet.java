package com.label.community.controller;

import com.label.community.dto.ActivityCreateRequest;
import com.label.community.dto.ActivityCheckinRequest;
import com.label.community.dto.ActivityReviewCreateRequest;
import com.label.community.security.AuthUser;
import com.label.community.service.ActivityService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = "/api/activities/*")
public class ActivityServlet extends BaseServlet {
    private final ActivityService activityService = new ActivityService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuthUser user = requiredUser();
        String path = request.getPathInfo();
        if (path == null || "/".equals(path)) {
            ok(response, activityService.listAll(user.getUserId()));
            return;
        }
        if ("/my".equals(path)) {
            ok(response, activityService.myActivities(user.getUserId()));
            return;
        }
        String[] segments = splitPath(path);
        if (segments.length == 2 && "checkin-qrcode".equals(segments[1])) {
            Long activityId = Long.valueOf(segments[0]);
            ok(response, activityService.checkinQr(user.getUserId(), activityId));
            return;
        }
        if (segments.length == 2 && "reviews".equals(segments[1])) {
            Long activityId = Long.valueOf(segments[0]);
            ok(response, activityService.reviews(activityId));
            return;
        }
        response.sendError(404);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuthUser user = requiredUser();
        String path = request.getPathInfo();
        if (path == null || "/".equals(path)) {
            ActivityCreateRequest body = readJson(request, ActivityCreateRequest.class);
            Long activityId = activityService.createActivity(user, body);
            ok(response, Map.of("activityId", activityId));
            return;
        }

        String[] segments = splitPath(path);
        if (segments.length == 2 && "signup".equals(segments[1])) {
            Long activityId = Long.valueOf(segments[0]);
            activityService.signup(user.getUserId(), activityId);
            okMessage(response, "报名成功");
            return;
        }
        if (segments.length == 2 && "checkin".equals(segments[1])) {
            Long activityId = Long.valueOf(segments[0]);
            ActivityCheckinRequest body = readJson(request, ActivityCheckinRequest.class);
            activityService.checkin(user.getUserId(), activityId, body.getToken());
            okMessage(response, "签到成功");
            return;
        }
        if (segments.length == 2 && "reviews".equals(segments[1])) {
            Long activityId = Long.valueOf(segments[0]);
            ActivityReviewCreateRequest body = readJson(request, ActivityReviewCreateRequest.class);
            Long reviewId = activityService.createReview(user.getUserId(), activityId, body);
            ok(response, Map.of("reviewId", reviewId));
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
