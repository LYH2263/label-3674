package com.label.community.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.label.community.common.JsonUtil;
import com.label.community.dao.ActivityDao;
import com.label.community.dao.MessageDao;
import com.label.community.dao.RepairDao;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationService {
    private final MessageDao messageDao = new MessageDao();
    private final RepairDao repairDao = new RepairDao();
    private final ActivityDao activityDao = new ActivityDao();

    public Map<String, Object> intelligentNotices(Long userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("personalizedRecommendations", personalizedRecommendations(userId));
        payload.put("weatherReminder", weatherReminder());
        payload.put("generatedAt", LocalDateTime.now());
        return payload;
    }

    private List<Map<String, Object>> personalizedRecommendations(Long userId) {
        int unread = messageDao.countUnread(userId);
        int pendingRepairs = repairDao.countPendingByUser(userId);
        int joinedActivities = activityDao.countByUser(userId);

        List<Map<String, Object>> list = new ArrayList<>();
        if (unread > 0) {
            list.add(item("MESSAGE", "消息待处理", "您有 " + unread + " 条未读消息，建议先进入消息中心处理。", 1));
        }
        if (pendingRepairs > 0) {
            list.add(item("REPAIR", "报修跟进", "当前有 " + pendingRepairs + " 条报修仍在处理中，建议查看最新进度。", 2));
        }
        if (joinedActivities > 0) {
            list.add(item("ACTIVITY", "活动提醒", "您已报名 " + joinedActivities + " 个活动，建议提前完成签到准备。", 3));
        }
        if (list.isEmpty()) {
            list.add(item("DISCOVERY", "个性化推荐", "当前事务较少，可前往邻里互动区浏览二手和技能交换信息。", 4));
        }
        return list;
    }

    private Map<String, Object> weatherReminder() {
        String latitude = System.getenv().getOrDefault("APP_WEATHER_LAT", "31.2304");
        String longitude = System.getenv().getOrDefault("APP_WEATHER_LON", "121.4737");
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude +
                "&longitude=" + longitude +
                "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
                "&timezone=Asia%2FShanghai&forecast_days=1";
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = JsonUtil.mapper().readTree(response.body());
            JsonNode daily = node.path("daily");

            double max = daily.path("temperature_2m_max").path(0).asDouble();
            double min = daily.path("temperature_2m_min").path(0).asDouble();
            int rain = daily.path("precipitation_probability_max").path(0).asInt();

            String level = rain >= 60 ? "HIGH" : (rain >= 30 ? "MEDIUM" : "LOW");
            String summary = "今日气温 " + toText(min) + "~" + toText(max) + "℃，降水概率约 " + rain + "%";
            String suggestion = rain >= 60 ? "建议携带雨具，外出活动注意防滑。" : "天气总体平稳，可正常安排社区活动。";

            Map<String, Object> reminder = new HashMap<>();
            reminder.put("level", level);
            reminder.put("summary", summary);
            reminder.put("suggestion", suggestion);
            return reminder;
        } catch (Exception ex) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("level", "UNKNOWN");
            fallback.put("summary", "天气服务暂时不可用");
            fallback.put("suggestion", "建议出门前查看本地天气应用，以便及时获取降雨提醒。");
            return fallback;
        }
    }

    private String toText(double value) {
        return BigDecimal.valueOf(value).setScale(1, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private Map<String, Object> item(String type, String title, String content, int priority) {
        Map<String, Object> row = new HashMap<>();
        row.put("type", type);
        row.put("title", title);
        row.put("content", content);
        row.put("priority", priority);
        return row;
    }
}
