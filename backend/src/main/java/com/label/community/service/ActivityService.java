package com.label.community.service;

import com.label.community.common.BusinessException;
import com.label.community.config.TransactionManager;
import com.label.community.dao.ActivityDao;
import com.label.community.dao.MessageDao;
import com.label.community.dto.ActivityCreateRequest;
import com.label.community.dto.ActivityReviewCreateRequest;
import com.label.community.model.ActivityItem;
import com.label.community.security.AuthUser;
import com.label.community.util.TimeUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityService {
    private static final DateTimeFormatter TOKEN_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final ActivityDao activityDao = new ActivityDao();
    private final MessageDao messageDao = new MessageDao();

    public List<ActivityItem> listAll(Long userId) {
        return activityDao.listAllWithJoinFlag(userId);
    }

    public List<ActivityItem> myActivities(Long userId) {
        return activityDao.listByUser(userId);
    }

    public Long createActivity(AuthUser user, ActivityCreateRequest request) {
        if (request == null) {
            throw new BusinessException(400, 40050, "参数不能为空");
        }
        String title = request.getTitle() == null ? "" : request.getTitle().trim();
        String desc = request.getDescription() == null ? "" : request.getDescription().trim();
        String location = request.getLocation() == null ? "" : request.getLocation().trim();
        if (title.length() < 3 || desc.length() < 8 || location.length() < 2) {
            throw new BusinessException(400, 40051, "活动信息填写不完整");
        }

        LocalDateTime start;
        try {
            start = TimeUtil.parseDateTime(request.getStartTime());
        } catch (Exception ex) {
            throw new BusinessException(400, 40060, "活动开始时间格式不正确");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, 40052, "活动开始时间必须晚于当前时间");
        }

        ActivityItem activity = new ActivityItem();
        activity.setTitle(title);
        activity.setDescription(desc);
        activity.setLocation(location);
        activity.setStartTime(start);
        activity.setOrganizerId(user.getUserId());
        activity.setOrganizerRole(user.getRole());

        return TransactionManager.runInTransaction(connection -> activityDao.insert(connection, activity));
    }

    public void signup(Long userId, Long activityId) {
        if (!activityDao.exists(activityId)) {
            throw new BusinessException(404, 40440, "活动不存在");
        }
        TransactionManager.runInTransaction(connection -> {
            activityDao.signup(connection, activityId, userId);
            messageDao.createMessage(connection, userId, null, "SYSTEM", "报名成功", "您已成功报名社区活动 #" + activityId + "。" );
            return null;
        });
    }

    public Map<String, Object> checkinQr(Long userId, Long activityId) {
        if (!activityDao.exists(activityId)) {
            throw new BusinessException(404, 40440, "活动不存在");
        }
        if (!activityDao.joined(activityId, userId)) {
            throw new BusinessException(400, 40053, "请先报名后再签到");
        }
        Map<String, Object> map = new HashMap<>();
        String token = buildCheckinToken(activityId, userId, LocalDateTime.now());
        map.put("activityId", activityId);
        map.put("token", token);
        map.put("signStatus", activityDao.signStatus(activityId, userId));
        map.put("checkinPayload", "activity:" + activityId + "|user:" + userId + "|token:" + token);
        return map;
    }

    public void checkin(Long userId, Long activityId, String token) {
        if (!activityDao.exists(activityId)) {
            throw new BusinessException(404, 40440, "活动不存在");
        }
        if (!activityDao.joined(activityId, userId)) {
            throw new BusinessException(400, 40053, "请先报名后再签到");
        }
        if (token == null || token.isBlank() || !verifyCheckinToken(activityId, userId, token)) {
            throw new BusinessException(400, 40054, "签到二维码无效或已过期");
        }
        TransactionManager.runInTransaction(connection -> {
            activityDao.checkin(connection, activityId, userId);
            messageDao.createMessage(connection, userId, null, "SYSTEM", "活动签到成功", "您已完成活动 #" + activityId + " 签到。");
            return null;
        });
    }

    public Long createReview(Long userId, Long activityId, ActivityReviewCreateRequest request) {
        if (!activityDao.exists(activityId)) {
            throw new BusinessException(404, 40440, "活动不存在");
        }
        if (!activityDao.joined(activityId, userId)) {
            throw new BusinessException(400, 40055, "仅报名用户可发布活动回顾");
        }
        if (!"SIGNED_IN".equals(activityDao.signStatus(activityId, userId))) {
            throw new BusinessException(400, 40056, "请先签到后再发布活动回顾");
        }
        if (request == null || request.getRating() == null) {
            throw new BusinessException(400, 40057, "请填写评分");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BusinessException(400, 40058, "评分需在1-5之间");
        }
        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (content.length() < 4 || content.length() > 255) {
            throw new BusinessException(400, 40059, "回顾内容需在4-255字之间");
        }

        return TransactionManager.runInTransaction(connection -> {
            Long reviewId = activityDao.insertReview(connection, activityId, userId, request.getRating(), content, request.getPhotoUrl());
            messageDao.createMessage(connection, userId, null, "SYSTEM", "活动回顾已发布", "您已成功发布活动 #" + activityId + " 的回顾。");
            return reviewId;
        });
    }

    public List<Map<String, Object>> reviews(Long activityId) {
        if (!activityDao.exists(activityId)) {
            throw new BusinessException(404, 40440, "活动不存在");
        }
        return activityDao.listReviews(activityId);
    }

    private String buildCheckinToken(Long activityId, Long userId, LocalDateTime now) {
        String day = TOKEN_DATE.format(now);
        String payload = activityId + ":" + userId + ":" + day;
        return hmac(payload);
    }

    private boolean verifyCheckinToken(Long activityId, Long userId, String token) {
        String today = buildCheckinToken(activityId, userId, LocalDateTime.now());
        String yesterday = buildCheckinToken(activityId, userId, LocalDateTime.now().minusDays(1));
        return token.equals(today) || token.equals(yesterday);
    }

    private String hmac(String payload) {
        try {
            String secret = System.getenv().getOrDefault("APP_ACTIVITY_CHECKIN_SECRET", System.getenv().getOrDefault("APP_JWT_SECRET", "smart-community-checkin-secret"));
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte item : bytes) {
                hex.append(String.format("%02x", item));
            }
            return hex.substring(0, 24);
        } catch (Exception ex) {
            throw new BusinessException(500, 50030, "签到码生成失败");
        }
    }
}
