package com.label.community.dao;

import com.label.community.config.DbConfig;
import com.label.community.model.RepairOrder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepairDao {
    private final DataSource dataSource = DbConfig.dataSource();

    public Long insert(Connection connection, RepairOrder repair) throws SQLException {
        String sql = "INSERT INTO repair_orders(user_id,title,description,image_url,status,assigned_provider_id) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, repair.getUserId());
            statement.setString(2, repair.getTitle());
            statement.setString(3, repair.getDescription());
            statement.setString(4, repair.getImageUrl());
            statement.setString(5, repair.getStatus());
            if (repair.getAssignedProviderId() == null) {
                statement.setNull(6, Types.BIGINT);
            } else {
                statement.setLong(6, repair.getAssignedProviderId());
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        return null;
    }

    public List<RepairOrder> listByUserId(Long userId) {
        String sql = "SELECT * FROM repair_orders WHERE user_id = ? ORDER BY id DESC";
        List<RepairOrder> items = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(map(resultSet));
                }
            }
            return items;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<RepairOrder> listPendingOrInProgress() {
        String sql = "SELECT * FROM repair_orders WHERE status IN ('PENDING','IN_PROGRESS') ORDER BY id DESC";
        List<RepairOrder> items = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                items.add(map(resultSet));
            }
            return items;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Optional<RepairOrder> findById(Long repairId) {
        String sql = "SELECT * FROM repair_orders WHERE id = ? LIMIT 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, repairId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void updateStatus(Connection connection, Long repairId, String status, Long providerId) throws SQLException {
        String sql = "UPDATE repair_orders SET status = ?, assigned_provider_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            if (providerId == null) {
                statement.setNull(2, Types.BIGINT);
            } else {
                statement.setLong(2, providerId);
            }
            statement.setLong(3, repairId);
            statement.executeUpdate();
        }
    }

    public void updateRating(Connection connection, Long repairId, Long userId, Integer rating, String comment) throws SQLException {
        String sql = "UPDATE repair_orders SET rating = ?, rating_comment = ? WHERE id = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, rating);
            statement.setString(2, comment);
            statement.setLong(3, repairId);
            statement.setLong(4, userId);
            statement.executeUpdate();
        }
    }

    public int countPendingByUser(Long userId) {
        String sql = "SELECT COUNT(1) FROM repair_orders WHERE user_id = ? AND status IN ('PENDING','IN_PROGRESS')";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
            return 0;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private RepairOrder map(ResultSet resultSet) throws SQLException {
        RepairOrder item = new RepairOrder();
        item.setId(resultSet.getLong("id"));
        item.setUserId(resultSet.getLong("user_id"));
        item.setTitle(resultSet.getString("title"));
        item.setDescription(resultSet.getString("description"));
        item.setImageUrl(resultSet.getString("image_url"));
        item.setStatus(resultSet.getString("status"));
        long providerId = resultSet.getLong("assigned_provider_id");
        item.setAssignedProviderId(resultSet.wasNull() ? null : providerId);
        int score = resultSet.getInt("rating");
        item.setRating(resultSet.wasNull() ? null : score);
        item.setRatingComment(resultSet.getString("rating_comment"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            item.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return item;
    }
}
