package com.label.community.service;

import com.label.community.common.BusinessException;
import com.label.community.config.RoleConstants;
import com.label.community.config.TransactionManager;
import com.label.community.dao.MessageDao;
import com.label.community.dao.RepairDao;
import com.label.community.dao.UserDao;
import com.label.community.dto.RepairRatingRequest;
import com.label.community.dto.RepairStatusUpdateRequest;
import com.label.community.model.RepairOrder;
import com.label.community.model.User;
import com.label.community.security.AuthUser;

import java.util.List;
import java.util.Map;

public class RepairService {
    private final RepairDao repairDao = new RepairDao();
    private final MessageDao messageDao = new MessageDao();
    private final UserDao userDao = new UserDao();

    public Long createRepair(Long userId, String title, String description, String imageUrl) {
        if (title == null || title.trim().length() < 4) {
            throw new BusinessException(400, 40040, "报修标题至少4个字符");
        }
        if (description == null || description.trim().length() < 8) {
            throw new BusinessException(400, 40041, "报修描述至少8个字符");
        }

        RepairOrder order = new RepairOrder();
        order.setUserId(userId);
        order.setTitle(title.trim());
        order.setDescription(description.trim());
        order.setImageUrl(imageUrl);
        order.setStatus("PENDING");

        return TransactionManager.runInTransaction(connection -> {
            Long repairId = repairDao.insert(connection, order);
            messageDao.createMessage(connection, userId, null, "SYSTEM", "报修提交成功", "工单 #" + repairId + " 已提交，物业将尽快受理。");
            return repairId;
        });
    }

    public List<RepairOrder> myRepairs(Long userId) {
        return repairDao.listByUserId(userId);
    }

    public List<RepairOrder> providerBoard() {
        return repairDao.listPendingOrInProgress();
    }

    public void updateStatus(AuthUser authUser, Long repairId, RepairStatusUpdateRequest request) {
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new BusinessException(400, 40042, "状态不能为空");
        }
        if (!(RoleConstants.PROPERTY_ADMIN.equals(authUser.getRole()) || RoleConstants.SERVICE_PROVIDER.equals(authUser.getRole()))) {
            throw new BusinessException(403, 40310, "当前角色无权更新报修状态");
        }
        RepairOrder repair = repairDao.findById(repairId).orElseThrow(() -> new BusinessException(404, 40420, "报修单不存在"));

        Long providerId = request.getAssignedProviderId();
        if (providerId != null) {
            User provider = userDao.findById(providerId).orElseThrow(() -> new BusinessException(404, 40421, "服务商不存在"));
            if (!RoleConstants.SERVICE_PROVIDER.equals(provider.getRole())) {
                throw new BusinessException(400, 40043, "指定人员不是服务商");
            }
        }

        TransactionManager.runInTransaction(connection -> {
            repairDao.updateStatus(connection, repairId, request.getStatus(), providerId);
            messageDao.createMessage(
                connection,
                repair.getUserId(),
                authUser.getUserId(),
                "SYSTEM",
                "报修状态更新",
                "工单 #" + repairId + " 状态已更新为「" + request.getStatus() + "」。"
            );
            return null;
        });
    }

    public void rateRepair(Long userId, Long repairId, RepairRatingRequest request) {
        if (request == null || request.getRating() == null) {
            throw new BusinessException(400, 40044, "评分不能为空");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BusinessException(400, 40045, "评分需在1-5之间");
        }
        RepairOrder repair = repairDao.findById(repairId).orElseThrow(() -> new BusinessException(404, 40422, "报修单不存在"));
        if (!repair.getUserId().equals(userId)) {
            throw new BusinessException(403, 40311, "仅报修发起人可评价");
        }
        TransactionManager.runInTransaction(connection -> {
            repairDao.updateRating(connection, repairId, userId, request.getRating(), request.getComment());
            if (repair.getAssignedProviderId() != null) {
                messageDao.createMessage(connection, repair.getAssignedProviderId(), userId, "PRIVATE", "服务评价通知", "工单 #" + repairId + " 收到新的服务评价。" );
            }
            return null;
        });
    }
}
