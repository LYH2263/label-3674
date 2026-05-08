package com.label.community.config;

import com.label.community.common.BusinessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionManager {
    @FunctionalInterface
    public interface TxCallable<T> {
        T call(Connection connection) throws SQLException;
    }

    private TransactionManager() {
    }

    public static <T> T runInTransaction(TxCallable<T> callable) {
        DataSource dataSource = DbConfig.dataSource();
        try (Connection connection = dataSource.getConnection()) {
            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                T result = callable.call(connection);
                connection.commit();
                connection.setAutoCommit(oldAutoCommit);
                return result;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new BusinessException(500, 50020, "事务执行失败: " + ex.getMessage());
        }
    }
}
