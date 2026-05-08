package com.label.community.dao;

import com.label.community.config.DbConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OperationLogDao {
    private final DataSource dataSource = DbConfig.dataSource();

    public void insert(Long userId, String username, String action, String path, String method, int statusCode, long durationMs) {
        String sql = "INSERT INTO operation_logs(user_id,username,action,path,method,status_code,duration_ms) VALUES(?,?,?,?,?,?,?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (userId == null) {
                statement.setNull(1, java.sql.Types.BIGINT);
            } else {
                statement.setLong(1, userId);
            }
            statement.setString(2, username);
            statement.setString(3, action);
            statement.setString(4, path);
            statement.setString(5, method);
            statement.setInt(6, statusCode);
            statement.setLong(7, durationMs);
            statement.executeUpdate();
        } catch (SQLException ignored) {
            // Do not interrupt main flow for log persistence failures.
        }
    }
}
