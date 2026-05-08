package com.label.community.dao;

import com.label.community.config.DbConfig;
import com.label.community.model.MessageItem;
import com.label.community.websocket.NotificationWebSocketEndpoint;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageDao {
    private final DataSource dataSource = DbConfig.dataSource();

    public List<MessageItem> listByReceiver(Long userId) {
        String sql = "SELECT * FROM messages WHERE receiver_id = ? ORDER BY id DESC";
        List<MessageItem> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(map(resultSet));
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void markRead(Connection connection, Long messageId, Long userId) throws SQLException {
        String sql = "UPDATE messages SET is_read = 1 WHERE id = ? AND receiver_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, messageId);
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    public void createMessage(Connection connection, Long receiverId, Long senderId, String type, String title, String content) throws SQLException {
        String sql = "INSERT INTO messages(receiver_id,sender_id,message_type,title,content,is_read) VALUES(?,?,?,?,?,0)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, receiverId);
            if (senderId == null) {
                statement.setNull(2, Types.BIGINT);
            } else {
                statement.setLong(2, senderId);
            }
            statement.setString(3, type);
            statement.setString(4, title);
            statement.setString(5, content);
            statement.executeUpdate();
        }
        NotificationWebSocketEndpoint.pushToUser(receiverId, Map.of(
            "event", "message_created",
            "title", title,
            "type", type,
            "time", LocalDateTime.now().toString()
        ));
    }

    public int countUnread(Long userId) {
        String sql = "SELECT COUNT(1) FROM messages WHERE receiver_id = ? AND is_read = 0";
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

    private MessageItem map(ResultSet resultSet) throws SQLException {
        MessageItem item = new MessageItem();
        item.setId(resultSet.getLong("id"));
        item.setReceiverId(resultSet.getLong("receiver_id"));
        Long senderId = resultSet.getLong("sender_id");
        item.setSenderId(resultSet.wasNull() ? null : senderId);
        item.setMessageType(resultSet.getString("message_type"));
        item.setTitle(resultSet.getString("title"));
        item.setContent(resultSet.getString("content"));
        item.setRead(resultSet.getBoolean("is_read"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }
        return item;
    }
}
