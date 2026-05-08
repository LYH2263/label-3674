package com.label.community.dao;

import com.label.community.config.DbConfig;
import com.label.community.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private final DataSource dataSource = DbConfig.dataSource();

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? LIMIT 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
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

    public Optional<User> findById(Long userId) {
        String sql = "SELECT * FROM users WHERE id = ? LIMIT 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
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

    public List<User> listByRole(String role) {
        String sql = "SELECT * FROM users WHERE role = ? AND status = 'ACTIVE' ORDER BY id DESC";
        List<User> users = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(map(resultSet));
                }
            }
            return users;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Long insert(Connection connection, User user) throws SQLException {
        String sql = "INSERT INTO users(username,password_hash,role,full_name,phone,email,real_name_verified,id_card_enc,status) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRole());
            statement.setString(4, user.getFullName());
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getEmail());
            statement.setBoolean(7, user.isRealNameVerified());
            statement.setString(8, user.getIdCardEnc());
            statement.setString(9, user.getStatus());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        return null;
    }

    public void updateProfile(Connection connection, Long userId, String fullName, String phone, String email) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, phone = ?, email = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fullName);
            statement.setString(2, phone);
            statement.setString(3, email);
            statement.setLong(4, userId);
            statement.executeUpdate();
        }
    }

    public void updatePasswordHash(Connection connection, Long userId, String hash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, hash);
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    private User map(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setRole(resultSet.getString("role"));
        user.setFullName(resultSet.getString("full_name"));
        user.setPhone(resultSet.getString("phone"));
        user.setEmail(resultSet.getString("email"));
        user.setRealNameVerified(resultSet.getBoolean("real_name_verified"));
        user.setIdCardEnc(resultSet.getString("id_card_enc"));
        user.setStatus(resultSet.getString("status"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        return user;
    }
}
