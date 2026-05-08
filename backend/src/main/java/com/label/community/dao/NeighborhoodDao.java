package com.label.community.dao;

import com.label.community.config.DbConfig;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NeighborhoodDao {
    private final DataSource dataSource = DbConfig.dataSource();

    public List<Map<String, Object>> list(String category, int offset, int limit) {
        StringBuilder sql = new StringBuilder(
            "SELECT p.id, p.user_id, p.category, p.title, p.content, p.price, p.contact, p.status, p.created_at, u.full_name " +
                "FROM neighborhood_posts p INNER JOIN users u ON p.user_id = u.id "
        );
        List<Object> params = new ArrayList<>();
        if (category != null && !category.isBlank()) {
            sql.append("WHERE p.category = ? ");
            params.add(category);
        }
        sql.append("ORDER BY p.id DESC LIMIT ?, ?");
        params.add(offset);
        params.add(limit);

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(map(resultSet));
                }
            }
            return result;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int count(String category) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM neighborhood_posts");
        List<Object> params = new ArrayList<>();
        if (category != null && !category.isBlank()) {
            sql.append(" WHERE category = ?");
            params.add(category);
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
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

    public Long insert(Connection connection, Long userId, String category, String title, String content, BigDecimal price, String contact) throws SQLException {
        String sql = "INSERT INTO neighborhood_posts(user_id, category, title, content, price, contact, status) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, userId);
            statement.setString(2, category);
            statement.setString(3, title);
            statement.setString(4, content);
            if (price == null) {
                statement.setNull(5, Types.DECIMAL);
            } else {
                statement.setBigDecimal(5, price);
            }
            statement.setString(6, contact);
            statement.setString(7, "OPEN");
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            return null;
        }
    }

    public boolean isOwner(Long postId, Long userId) {
        String sql = "SELECT 1 FROM neighborhood_posts WHERE id = ? AND user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, postId);
            statement.setLong(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void close(Connection connection, Long postId, Long userId) throws SQLException {
        String sql = "UPDATE neighborhood_posts SET status = 'CLOSED' WHERE id = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, postId);
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    private void bind(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }

    private Map<String, Object> map(ResultSet resultSet) throws SQLException {
        Map<String, Object> item = new HashMap<>();
        item.put("id", resultSet.getLong("id"));
        item.put("userId", resultSet.getLong("user_id"));
        item.put("category", resultSet.getString("category"));
        item.put("title", resultSet.getString("title"));
        item.put("content", resultSet.getString("content"));
        item.put("price", resultSet.getBigDecimal("price"));
        item.put("contact", resultSet.getString("contact"));
        item.put("status", resultSet.getString("status"));
        item.put("publisherName", resultSet.getString("full_name"));
        item.put("createdAt", resultSet.getTimestamp("created_at").toLocalDateTime());
        return item;
    }
}
