package com.label.community.service;

import com.label.community.common.BusinessException;
import com.label.community.config.TransactionManager;
import com.label.community.dao.NeighborhoodDao;
import com.label.community.dto.NeighborhoodPostCreateRequest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NeighborhoodService {
    private static final Set<String> ALLOWED_CATEGORY = Set.of(
        "NEIGHBOR_CIRCLE",
        "SECOND_HAND",
        "SKILL_SWAP",
        "LOST_FOUND"
    );

    private final NeighborhoodDao neighborhoodDao = new NeighborhoodDao();

    public Map<String, Object> list(String category, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 50);
        int offset = (safePage - 1) * safePageSize;

        if (category != null && !category.isBlank() && !ALLOWED_CATEGORY.contains(category)) {
            throw new BusinessException(400, 40080, "互动分类不合法");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("records", neighborhoodDao.list(category, offset, safePageSize));
        payload.put("total", neighborhoodDao.count(category));
        payload.put("page", safePage);
        payload.put("pageSize", safePageSize);
        return payload;
    }

    public Long createPost(Long userId, NeighborhoodPostCreateRequest request) {
        if (request == null) {
            throw new BusinessException(400, 40081, "发布内容不能为空");
        }
        String category = request.getCategory() == null ? "" : request.getCategory().trim();
        String title = request.getTitle() == null ? "" : request.getTitle().trim();
        String content = request.getContent() == null ? "" : request.getContent().trim();
        String contact = request.getContact() == null ? "" : request.getContact().trim();
        BigDecimal price = request.getPrice();

        if (!ALLOWED_CATEGORY.contains(category)) {
            throw new BusinessException(400, 40082, "互动分类不合法");
        }
        if (title.length() < 2 || title.length() > 120) {
            throw new BusinessException(400, 40083, "标题长度需在2-120字符之间");
        }
        if (content.length() < 4 || content.length() > 500) {
            throw new BusinessException(400, 40084, "内容长度需在4-500字符之间");
        }
        if (!contact.isBlank() && contact.length() > 80) {
            throw new BusinessException(400, 40085, "联系方式长度不能超过80字符");
        }
        if (price != null && price.signum() < 0) {
            throw new BusinessException(400, 40086, "价格不能为负数");
        }

        return TransactionManager.runInTransaction(connection ->
            neighborhoodDao.insert(connection, userId, category, title, content, price, contact.isBlank() ? null : contact)
        );
    }

    public void closePost(Long userId, Long postId) {
        if (!neighborhoodDao.isOwner(postId, userId)) {
            throw new BusinessException(403, 40331, "仅发布者可关闭该帖子");
        }
        TransactionManager.runInTransaction(connection -> {
            neighborhoodDao.close(connection, postId, userId);
            return null;
        });
    }
}
