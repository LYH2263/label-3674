package com.label.community.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public final class DbConfig {
    private static volatile HikariDataSource dataSource;

    private DbConfig() {
    }

    public static DataSource dataSource() {
        if (dataSource == null) {
            synchronized (DbConfig.class) {
                if (dataSource == null) {
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl(System.getenv().getOrDefault(
                        "APP_DB_URL",
                        "jdbc:mysql://localhost:3306/smart_community?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8"
                    ));
                    config.setUsername(System.getenv().getOrDefault("APP_DB_USER", "smart_user"));
                    config.setPassword(System.getenv().getOrDefault("APP_DB_PASSWORD", "smart_pass_3674"));
                    config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                    config.setMaximumPoolSize(12);
                    config.setMinimumIdle(2);
                    config.setConnectionTimeout(8000);
                    config.setIdleTimeout(30000);
                    config.setMaxLifetime(120000);
                    dataSource = new HikariDataSource(config);
                }
            }
        }
        return dataSource;
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
