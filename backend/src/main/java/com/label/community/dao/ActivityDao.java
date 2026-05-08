package com.label.community.dao;

import com.label.community.config.DbConfig;
import com.label.community.model.ActivityItem;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityDao {
    private final DataSource dataSource = DbConfig.dataSource();

    public List<ActivityItem> listAllWithJoinFlag(Long userId) {
        String sql = "SELECT a.*, CASE WHEN s.id IS NULL THEN 0 ELSE 1 END AS joined " +
            "FROM activities a LEFT JOIN activity_signups s ON a.id = s.activity_id AND s.user_id = ? " +
            "ORDER BY a.start_time ASC";
        List<ActivityItem> items = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ActivityItem item = map(resultSet);
                    item.setJoined(resultSet.getInt("joined") == 1);
                    items.add(item);
                }
            }
            return items;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<ActivityItem> listByUser(Long userId) {
        String sql = "SELECT a.* FROM activities a INNER JOIN activity_signups s ON a.id = s.activity_id WHERE s.user_id = ? ORDER BY a.start_time ASC";
        List<ActivityItem> items = new ArrayList<>();
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

    public Long insert(Connection connection, ActivityItem activity) throws SQLException {
        String sql = "INSERT INTO activities(title,description,location,start_time,organizer_id,organizer_role) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, activity.getTitle());
            statement.setString(2, activity.getDescription());
            statement.setString(3, activity.getLocation());
            statement.setTimestamp(4, Timestamp.valueOf(activity.getStartTime()));
            statement.setLong(5, activity.getOrganizerId());
            statement.setString(6, activity.getOrganizerRole());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        return null;
    }

    public void signup(Connection connection, Long activityId, Long userId) throws SQLException {
        String sql = "INSERT INTO activity_signups(activity_id,user_id,sign_status,signed_at) VALUES(?,?,?,NULL) ON DUPLICATE KEY UPDATE sign_status='REGISTERED', signed_at=NULL";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, activityId);
            statement.setLong(2, userId);
            statement.setString(3, "REGISTERED");
            statement.executeUpdate();
        }
    }

    public boolean exists(Long activityId) {
        String sql = "SELECT 1 FROM activities WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, activityId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean joined(Long activityId, Long userId) {
        String sql = "SELECT 1 FROM activity_signups WHERE activity_id = ? AND user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, activityId);
            statement.setLong(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String signStatus(Long activityId, Long userId) {
        String sql = "SELECT sign_status FROM activity_signups WHERE activity_id = ? AND user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, activityId);
            statement.setLong(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("sign_status");
                }
                return "NOT_SIGNED";
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void checkin(Connection connection, Long activityId, Long userId) throws SQLException {
        String sql = "UPDATE activity_signups SET sign_status = 'SIGNED_IN', signed_at = NOW() WHERE activity_id = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, activityId);
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    public Long insertReview(Connection connection, Long activityId, Long userId, Integer rating, String content, String photoUrl) throws SQLException {
        String sql = "INSERT INTO activity_reviews(activity_id, user_id, rating, content, photo_url) VALUES(?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, activityId);
            statement.setLong(2, userId);
            statement.setInt(3, rating);
            statement.setString(4, content);
            if (photoUrl == null || photoUrl.isBlank()) {
                statement.setNull(5, Types.VARCHAR);
            } else {
                statement.setString(5, photoUrl);
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            return null;
        }
    }

    public List<Map<String, Object>> listReviews(Long activityId) {
        String sql = "SELECT r.id, r.rating, r.content, r.photo_url, r.created_at, u.full_name " +
            "FROM activity_reviews r INNER JOIN users u ON r.user_id = u.id WHERE r.activity_id = ? ORDER BY r.id DESC";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, activityId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", resultSet.getLong("id"));
                    row.put("rating", resultSet.getInt("rating"));
                    row.put("content", resultSet.getString("content"));
                    row.put("photoUrl", resultSet.getString("photo_url"));
                    row.put("reviewerName", resultSet.getString("full_name"));
                    row.put("createdAt", resultSet.getTimestamp("created_at").toLocalDateTime());
                    list.add(row);
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int countByUser(Long userId) {
        String sql = "SELECT COUNT(1) FROM activity_signups WHERE user_id = ?";
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

    private ActivityItem map(ResultSet resultSet) throws SQLException {
        ActivityItem item = new ActivityItem();
        item.setId(resultSet.getLong("id"));
        item.setTitle(resultSet.getString("title"));
        item.setDescription(resultSet.getString("description"));
        item.setLocation(resultSet.getString("location"));
        Timestamp start = resultSet.getTimestamp("start_time");
        if (start != null) {
            item.setStartTime(start.toLocalDateTime());
        }
        item.setOrganizerId(resultSet.getLong("organizer_id"));
        item.setOrganizerRole(resultSet.getString("organizer_role"));
        return item;
    }
}
