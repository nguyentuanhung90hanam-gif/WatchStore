package com.watchstore.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Future-ready SQL Server connection. The current demo uses mock repositories. */
public final class DBContext {
    private DBContext() {}

    public static Connection getConnection() throws SQLException {
        String url = env("WATCHSTORE_DB_URL", "jdbc:sqlserver://localhost:1433;databaseName=WatchStore;encrypt=true;trustServerCertificate=true");
        String user = env("WATCHSTORE_DB_USER", "sa");
        String password = env("WATCHSTORE_DB_PASSWORD", "123456");
        return DriverManager.getConnection(url, user, password);
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}
