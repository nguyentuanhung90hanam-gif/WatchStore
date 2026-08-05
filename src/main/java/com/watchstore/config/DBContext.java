package com.watchstore.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Cung cấp kết nối JDBC đến SQL Server.
 */
public final class DBContext {

    private static final String DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    private DBContext() {}

    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy SQL Server JDBC driver trong classpath!");
        }
    }

    /**
     * Lấy một Connection mới từ DriverManager.
     */
    public static Connection getConnection() throws SQLException {
        String url = env("WATCHSTORE_DB_URL",
                "jdbc:sqlserver://localhost:1433;databaseName=WatchStore;encrypt=true;trustServerCertificate=true");
        String user     = env("WATCHSTORE_DB_USER",     "sa");
        String password = env("WATCHSTORE_DB_PASSWORD", "123456");
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Kiểm tra nhanh xem hiện tại có kết nối được tới SQL Server hay không.
     * @return true nếu kết nối thành công, false nếu thất bại.
     */
    public static boolean isConnected() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
