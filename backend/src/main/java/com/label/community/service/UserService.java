package com.label.community.service;

import com.label.community.common.BusinessException;
import com.label.community.config.HotDataCache;
import com.label.community.config.TransactionManager;
import com.label.community.dao.*;
import com.label.community.dto.ProfileUpdateRequest;
import com.label.community.listener.OnlineUserTracker;
import com.label.community.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
    private final UserDao userDao = new UserDao();
    private final RepairDao repairDao = new RepairDao();
    private final ActivityDao activityDao = new ActivityDao();
    private final MessageDao messageDao = new MessageDao();
    private final PropertyDao propertyDao = new PropertyDao();

    public Map<String, Object> profile(Long userId) {
        User user = userDao.findById(userId).orElseThrow(() -> new BusinessException(404, 40402, "用户不存在"));
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("role", user.getRole());
        map.put("fullName", user.getFullName());
        map.put("phone", user.getPhone());
        map.put("email", user.getEmail());
        map.put("realNameVerified", user.isRealNameVerified());
        map.put("createdAt", user.getCreatedAt());
        return map;
    }

    public Map<String, Object> updateProfile(Long userId, ProfileUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(400, 40030, "更新参数不能为空");
        }
        String fullName = request.getFullName() == null ? "" : request.getFullName().trim();
        if (fullName.length() < 2 || fullName.length() > 32) {
            throw new BusinessException(400, 40031, "姓名长度需在2-32位之间");
        }
        TransactionManager.runInTransaction(connection -> {
            userDao.updateProfile(connection, userId, fullName, request.getPhone(), request.getEmail());
            return null;
        });
        return profile(userId);
    }

    public Map<String, Object> personalCenter(Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("profile", profile(userId));
        map.put("pendingRepairCount", repairDao.countPendingByUser(userId));
        map.put("joinedActivityCount", activityDao.countByUser(userId));
        map.put("unreadMessageCount", messageDao.countUnread(userId));
        map.put("notices", HotDataCache.notices());
        map.put("onlineUsers", OnlineUserTracker.onlineUsers());
        map.put("uptimeMinutes", OnlineUserTracker.uptimeMinutes());
        return map;
    }

    public Map<String, Object> propertyServices(Long userId) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> payments = propertyDao.listPaymentsByUser(userId);
        map.put("payments", payments);
        map.put("parkingSlots", propertyDao.listParkingSlots());
        map.put("visitors", propertyDao.listVisitorsByResident(userId));
        return map;
    }
}
