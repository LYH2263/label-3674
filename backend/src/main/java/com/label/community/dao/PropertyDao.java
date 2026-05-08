package com.label.community.dao;

import com.label.community.config.DbConfig;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class PropertyDao {
    private final DataSource dataSource = DbConfig.dataSource();

    public List<Map<String, Object>> listPaymentsByUser(Long userId) {
        String sql = "SELECT id, amount, pay_status, paid_at, created_at FROM property_payments WHERE user_id = ? ORDER BY id DESC";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", resultSet.getLong("id"));
                    item.put("amount", resultSet.getBigDecimal("amount"));
                    item.put("payStatus", resultSet.getString("pay_status"));
                    Timestamp paidAt = resultSet.getTimestamp("paid_at");
                    item.put("paidAt", paidAt == null ? null : paidAt.toLocalDateTime());
                    item.put("createdAt", resultSet.getTimestamp("created_at").toLocalDateTime());
                    list.add(item);
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Map<String, Object> findPayment(Long userId, Long paymentId) {
        String sql = "SELECT id, amount, pay_status, paid_at FROM property_payments WHERE id = ? AND user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, paymentId);
            statement.setLong(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Map<String, Object> item = new HashMap<>();
                item.put("id", resultSet.getLong("id"));
                item.put("amount", resultSet.getBigDecimal("amount"));
                item.put("payStatus", resultSet.getString("pay_status"));
                Timestamp paidAt = resultSet.getTimestamp("paid_at");
                item.put("paidAt", paidAt == null ? null : paidAt.toLocalDateTime());
                return item;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void createPaymentTransaction(Connection connection, Long paymentId, Long userId, String payNo, String channel, java.math.BigDecimal amount) throws SQLException {
        String sql = "INSERT INTO property_payment_transactions(payment_id, user_id, pay_no, amount, channel, status) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, paymentId);
            statement.setLong(2, userId);
            statement.setString(3, payNo);
            statement.setBigDecimal(4, amount);
            statement.setString(5, channel);
            statement.setString(6, "INITIATED");
            statement.executeUpdate();
        }
    }

    public Map<String, Object> findTransaction(Long userId, Long paymentId, String payNo) {
        String sql = "SELECT id, pay_no, amount, channel, status, paid_at, created_at " +
            "FROM property_payment_transactions WHERE user_id = ? AND payment_id = ? AND pay_no = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, paymentId);
            statement.setString(3, payNo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapTx(resultSet);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Map<String, Object>> listTransactionsByUser(Long userId) {
        String sql = "SELECT id, payment_id, pay_no, amount, channel, status, paid_at, created_at " +
            "FROM property_payment_transactions WHERE user_id = ? ORDER BY id DESC";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> item = mapTx(resultSet);
                    item.put("paymentId", resultSet.getLong("payment_id"));
                    list.add(item);
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void markTransactionSuccess(Connection connection, String payNo) throws SQLException {
        String sql = "UPDATE property_payment_transactions SET status = 'SUCCESS', paid_at = NOW() WHERE pay_no = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, payNo);
            statement.executeUpdate();
        }
    }

    public void markPaymentSuccess(Connection connection, Long paymentId, Long userId) throws SQLException {
        String sql = "UPDATE property_payments SET pay_status = 'PAID', paid_at = NOW() WHERE id = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, paymentId);
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    public List<Map<String, Object>> listParkingSlots() {
        String sql = "SELECT slot_no, status, owner_user_id FROM parking_slots ORDER BY slot_no";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("slotNo", resultSet.getString("slot_no"));
                item.put("status", resultSet.getString("status"));
                long owner = resultSet.getLong("owner_user_id");
                item.put("ownerUserId", resultSet.wasNull() ? null : owner);
                list.add(item);
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void createVisitor(Connection connection, Long residentId, String visitorName, LocalDateTime visitTime) throws SQLException {
        String sql = "INSERT INTO visitor_records(resident_id, visitor_name, visit_time, status) VALUES(?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, residentId);
            statement.setString(2, visitorName);
            statement.setTimestamp(3, Timestamp.valueOf(visitTime));
            statement.setString(4, "APPROVED");
            statement.executeUpdate();
        }
    }

    public List<Map<String, Object>> listVisitorsByResident(Long residentId) {
        String sql = "SELECT id, visitor_name, visit_time, status FROM visitor_records WHERE resident_id = ? ORDER BY visit_time DESC";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, residentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", resultSet.getLong("id"));
                    item.put("visitorName", resultSet.getString("visitor_name"));
                    item.put("visitTime", resultSet.getTimestamp("visit_time").toLocalDateTime());
                    item.put("status", resultSet.getString("status"));
                    list.add(item);
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Map<String, Object> mapTx(ResultSet resultSet) throws SQLException {
        Map<String, Object> item = new HashMap<>();
        item.put("id", resultSet.getLong("id"));
        item.put("payNo", resultSet.getString("pay_no"));
        item.put("amount", resultSet.getBigDecimal("amount"));
        item.put("channel", resultSet.getString("channel"));
        item.put("status", resultSet.getString("status"));
        Timestamp paidAt = resultSet.getTimestamp("paid_at");
        item.put("paidAt", paidAt == null ? null : paidAt.toLocalDateTime());
        item.put("createdAt", resultSet.getTimestamp("created_at").toLocalDateTime());
        return item;
    }
}
