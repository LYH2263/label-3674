package com.label.community.dao;

import com.label.community.config.DbConfig;
import com.label.community.model.NoticeItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NoticeDao {
    private final DataSource dataSource = DbConfig.dataSource();

    public List<NoticeItem> latest(int limit) {
        String sql = "SELECT * FROM notices ORDER BY id DESC LIMIT ?";
        List<NoticeItem> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    NoticeItem item = new NoticeItem();
                    item.setId(resultSet.getLong("id"));
                    item.setTitle(resultSet.getString("title"));
                    item.setContent(resultSet.getString("content"));
                    item.setEmergency(resultSet.getBoolean("is_emergency"));
                    Timestamp createdAt = resultSet.getTimestamp("created_at");
                    if (createdAt != null) {
                        item.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    list.add(item);
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
