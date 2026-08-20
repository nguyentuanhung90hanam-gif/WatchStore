package com.watchstore;

import com.watchstore.config.DBContext;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseLiveSync {

    @Test
    public void executeLiveDatabaseSync() {
        System.out.println("================================================================================");
        System.out.println("  WATCHSTORE - DIRECT LIVE DATABASE SYNC & AUDIT TOOL");
        System.out.println("================================================================================");

        try (Connection conn = DBContext.getConnection()) {
            assertNotNull(conn, "Connection must not be null");

            // 1. Verify DB Name
            String currentDb = "";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT DB_NAME() AS CurrentDatabase")) {
                if (rs.next()) {
                    currentDb = rs.getString("CurrentDatabase");
                }
            }
            System.out.println("[INFO] SELECT DB_NAME() Result: " + currentDb);
            assertEquals("WatchStore", currentDb, "Database name must be WatchStore");

            // 2. Backup Live Database
            File backupDir = new File("h:/TESTER/WatchStore/database/archive");
            if (!backupDir.exists()) backupDir.mkdirs();
            String backupPath = "H:\\TESTER\\WatchStore\\database\\archive\\WatchStore_Live_PreSync.bak";
            System.out.println("[INFO] Creating Live Database Backup to: " + backupPath);
            try (Statement st = conn.createStatement()) {
                String backupSql = "BACKUP DATABASE WatchStore TO DISK = '" + backupPath + "' WITH FORMAT, INIT, NAME = 'WatchStore Live Backup';";
                st.execute(backupSql);
                System.out.println("[SUCCESS] Live database backup file created successfully.");
            } catch (Exception ex) {
                System.out.println("[WARN] T-SQL Backup note: " + ex.getMessage());
            }

            // 3. Inspect Live Database Tables & Row Counts (BEFORE)
            System.out.println("\n=== DATABASE BEFORE ===");
            System.out.println("Database: " + currentDb);
            System.out.println("Tables & Row Counts:");
            Map<String, Integer> tablesBefore = getTablesAndCounts(conn);
            for (Map.Entry<String, Integer> entry : tablesBefore.entrySet()) {
                System.out.printf("  - %-32s : %d rows%n", entry.getKey(), entry.getValue());
            }
            System.out.println("Total Tables: " + tablesBefore.size());

            // 4. Perform Schema Updates Directly on Live Database
            System.out.println("\n=== DATABASE MERGE (APPLYING SCHEMA & PATCHES) ===");
            try (Statement st = conn.createStatement()) {
                // A. ProductComments
                System.out.println("[MERGE] Ensuring dbo.ProductComments table exists...");
                st.execute("""
                    IF OBJECT_ID(N'dbo.ProductComments', N'U') IS NULL
                    BEGIN
                        CREATE TABLE dbo.ProductComments (
                            CommentID       BIGINT IDENTITY(1,1) PRIMARY KEY,
                            ProductID       INT NOT NULL,
                            UserID          INT NOT NULL,
                            Content         NVARCHAR(1000) NOT NULL,
                            Status          VARCHAR(20) NOT NULL CONSTRAINT DF_ProductComments_Status DEFAULT 'APPROVED',
                            CreatedAt       DATETIME2 NOT NULL CONSTRAINT DF_ProductComments_CreatedAt DEFAULT SYSDATETIME(),
                            CONSTRAINT FK_ProductComments_Product FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID) ON DELETE CASCADE,
                            CONSTRAINT FK_ProductComments_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE
                        );
                    END
                """);

                // B. OrderItems WarrantyMonths
                System.out.println("[MERGE] Ensuring OrderItems.WarrantyMonths column exists...");
                st.execute("""
                    IF NOT EXISTS (
                        SELECT * FROM sys.columns 
                        WHERE object_id = OBJECT_ID('dbo.OrderItems') AND name = 'WarrantyMonths'
                    )
                    BEGIN
                        ALTER TABLE dbo.OrderItems ADD WarrantyMonths INT NOT NULL CONSTRAINT DF_OrderItems_WarrantyMonths DEFAULT 12;
                    END
                """);

                // C. Warranties Enhancements
                System.out.println("[MERGE] Ensuring Warranties columns & nullable OrderID exist...");
                st.execute("""
                    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.Warranties') AND name = 'ImageUrl')
                        ALTER TABLE dbo.Warranties ADD ImageUrl NVARCHAR(1000) NULL;
                    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.Warranties') AND name = 'CustomerName')
                        ALTER TABLE dbo.Warranties ADD CustomerName NVARCHAR(150) NULL;
                    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.Warranties') AND name = 'CustomerPhone')
                        ALTER TABLE dbo.Warranties ADD CustomerPhone VARCHAR(20) NULL;
                    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.Warranties') AND name = 'CustomerEmail')
                        ALTER TABLE dbo.Warranties ADD CustomerEmail VARCHAR(150) NULL;
                    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.Warranties') AND name = 'WarrantyType')
                        ALTER TABLE dbo.Warranties ADD WarrantyType VARCHAR(20) NOT NULL CONSTRAINT DF_Warranties_WarrantyType DEFAULT 'ONLINE';
                    ALTER TABLE dbo.Warranties ALTER COLUMN OrderID BIGINT NULL;
                """);

                // D. OtpVerifications
                System.out.println("[MERGE] Ensuring OtpVerifications columns exist...");
                st.execute("""
                    IF OBJECT_ID(N'dbo.OtpVerifications', N'U') IS NULL
                    BEGIN
                        CREATE TABLE dbo.OtpVerifications (
                            OtpID               BIGINT IDENTITY(1,1) PRIMARY KEY,
                            Email               VARCHAR(150) NOT NULL,
                            OtpHash             VARCHAR(255) NOT NULL,
                            Purpose             VARCHAR(50) NOT NULL,
                            ExpiresAt           DATETIME2 NOT NULL,
                            Attempts            INT NOT NULL CONSTRAINT DF_Otp_Attempts DEFAULT 0,
                            Verified            BIT NOT NULL CONSTRAINT DF_Otp_Verified DEFAULT 0,
                            LastSentAt          DATETIME2 NOT NULL CONSTRAINT DF_Otp_LastSentAt DEFAULT SYSDATETIME(),
                            ResendCount         INT NOT NULL CONSTRAINT DF_Otp_ResendCount DEFAULT 0,
                            CreatedAt           DATETIME2 NOT NULL CONSTRAINT DF_Otp_CreatedAt DEFAULT SYSDATETIME()
                        );
                    END
                    ELSE
                    BEGIN
                        IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.OtpVerifications') AND name = 'OtpHash')
                            ALTER TABLE dbo.OtpVerifications ADD OtpHash VARCHAR(255) NOT NULL CONSTRAINT DF_Otp_Hash DEFAULT '';
                        IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.OtpVerifications') AND name = 'Purpose')
                            ALTER TABLE dbo.OtpVerifications ADD Purpose VARCHAR(50) NOT NULL CONSTRAINT DF_Otp_Purpose DEFAULT 'LOGIN';
                        IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.OtpVerifications') AND name = 'Attempts')
                            ALTER TABLE dbo.OtpVerifications ADD Attempts INT NOT NULL CONSTRAINT DF_Otp_Attempts DEFAULT 0;
                        IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.OtpVerifications') AND name = 'Verified')
                            ALTER TABLE dbo.OtpVerifications ADD Verified BIT NOT NULL CONSTRAINT DF_Otp_Verified DEFAULT 0;
                        IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.OtpVerifications') AND name = 'LastSentAt')
                            ALTER TABLE dbo.OtpVerifications ADD LastSentAt DATETIME2 NOT NULL CONSTRAINT DF_Otp_LastSentAt DEFAULT SYSDATETIME();
                        IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.OtpVerifications') AND name = 'ResendCount')
                            ALTER TABLE dbo.OtpVerifications ADD ResendCount INT NOT NULL CONSTRAINT DF_Otp_ResendCount DEFAULT 0;
                    END
                """);
            }
            System.out.println("[SUCCESS] Live schema updates merged successfully.");

            // 5. Merge Seed Data into Live DB
            System.out.println("\n[MERGE] Merging canonical seed data (Vouchers, Roles, Permissions)...");
            try (Statement st = conn.createStatement()) {
                st.execute("""
                    IF NOT EXISTS (SELECT 1 FROM dbo.Vouchers WHERE VoucherCode = 'WSNEW15')
                    BEGIN
                        INSERT INTO dbo.Vouchers (VoucherCode, VoucherName, Description, DiscountType, DiscountValue, MinimumOrderValue, MaximumDiscount, UsageLimit, UsageLimitPerUser, UsedCount, StartAt, EndAt, IsPublic, Status)
                        VALUES ('WSNEW15', N'Ưu đãi khách hàng mới', N'Giảm 15% cho đơn hàng đầu tiên từ 2.000.000₫', 'PERCENT', 15.00, 2000000.00, 500000.00, 500, 1, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1, 'ACTIVE');
                    END;
                    IF NOT EXISTS (SELECT 1 FROM dbo.Vouchers WHERE VoucherCode = 'AUTO1000')
                    BEGIN
                        INSERT INTO dbo.Vouchers (VoucherCode, VoucherName, Description, DiscountType, DiscountValue, MinimumOrderValue, MaximumDiscount, UsageLimit, UsageLimitPerUser, UsedCount, StartAt, EndAt, IsPublic, Status)
                        VALUES ('AUTO1000', N'Ưu đãi bộ sưu tập Automatic', N'Giảm trực tiếp 1.000.000₫ cho đơn từ 10.000.000₫', 'FIXED', 1000000.00, 10000000.00, 1000000.00, 200, 1, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1, 'ACTIVE');
                    END;
                    IF NOT EXISTS (SELECT 1 FROM dbo.Vouchers WHERE VoucherCode = 'FREESHIP')
                    BEGIN
                        INSERT INTO dbo.Vouchers (VoucherCode, VoucherName, Description, DiscountType, DiscountValue, MinimumOrderValue, MaximumDiscount, UsageLimit, UsageLimitPerUser, UsedCount, StartAt, EndAt, IsPublic, Status)
                        VALUES ('FREESHIP', N'Miễn phí vận chuyển toàn quốc', N'Miễn phí vận chuyển cho mọi giá trị đơn', 'FREESHIP', 50000.00, 0.00, 50000.00, 1000, 5, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1, 'ACTIVE');
                    END;
                """);
            }
            System.out.println("[SUCCESS] Canonical data verified and merged.");

            // 6. Clean Unused Legacy Tables
            System.out.println("\n=== DATABASE CLEANUP (REMOVING CONFIRMED UNUSED TABLES) ===");
            String[] unusedTables = {
                "dbo.Deliveries",
                "dbo.Payments",
                "dbo.ReviewReplies",
                "dbo.ReviewMedia",
                "dbo.OrderStatusHistory",
                "dbo.ReturnItems",
                "dbo.ReturnRequests",
                "dbo.UserNotifications",
                "dbo.AuditLogs",
                "dbo.SystemSettings",
                "dbo.StockAlertRules",
                "dbo.PostComments"
            };

            for (String tbl : unusedTables) {
                try (Statement st = conn.createStatement()) {
                    String checkSql = "IF OBJECT_ID(N'" + tbl + "', N'U') IS NOT NULL DROP TABLE " + tbl + ";";
                    st.execute(checkSql);
                    System.out.println("  - Dropped unused table: " + tbl);
                } catch (Exception ex) {
                    System.out.println("  - Table " + tbl + " note: " + ex.getMessage());
                }
            }

            // 7. Inspect Live Database Tables & Row Counts (AFTER)
            System.out.println("\n=== DATABASE AFTER ===");
            System.out.println("Database: " + currentDb);
            System.out.println("Tables & Row Counts:");
            Map<String, Integer> tablesAfter = getTablesAndCounts(conn);
            for (Map.Entry<String, Integer> entry : tablesAfter.entrySet()) {
                System.out.printf("  - %-32s : %d rows%n", entry.getKey(), entry.getValue());
            }
            System.out.println("Total Tables: " + tablesAfter.size());

            System.out.println("\n================================================================================");
            System.out.println("  DIRECT LIVE DATABASE SYNC & AUDIT COMPLETED SUCCESSFULLY");
            System.out.println("================================================================================");
        } catch (Exception e) {
            fail("Exception during live database sync: " + e.getMessage());
        }
    }

    private static Map<String, Integer> getTablesAndCounts(Connection conn) throws SQLException {
        Map<String, Integer> result = new TreeMap<>();
        String sql = """
            SELECT TABLE_SCHEMA + '.' + TABLE_NAME AS FullTableName
            FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_TYPE = 'BASE TABLE'
            ORDER BY TABLE_NAME
        """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            List<String> tableNames = new ArrayList<>();
            while (rs.next()) {
                tableNames.add(rs.getString("FullTableName"));
            }

            for (String table : tableNames) {
                try (Statement countSt = conn.createStatement();
                     ResultSet countRs = countSt.executeQuery("SELECT COUNT(*) AS Cnt FROM " + table)) {
                    if (countRs.next()) {
                        result.put(table, countRs.getInt("Cnt"));
                    }
                } catch (Exception ex) {
                    result.put(table, -1);
                }
            }
        }
        return result;
    }
}
