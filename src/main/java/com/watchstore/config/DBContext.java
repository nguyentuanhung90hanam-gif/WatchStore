package com.watchstore.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** SQL Server connection via JDBC. */
public final class DBContext {
    private DBContext() {}

    static {
        // Load SQL Server JDBC driver explicitly so DriverManager can find it
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                "mssql-jdbc driver not found on classpath: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        String url  = env("WATCHSTORE_DB_URL",
                "jdbc:sqlserver://localhost:1433;databaseName=WatchStore;encrypt=true;trustServerCertificate=true");
        String user = env("WATCHSTORE_DB_USER",     "sa");
        String pass = env("WATCHSTORE_DB_PASSWORD", "123456");
        return DriverManager.getConnection(url, user, pass);
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
