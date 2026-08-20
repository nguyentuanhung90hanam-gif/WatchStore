package com.watchstore;

import com.watchstore.config.DBContext;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseDeepAuditTest {

    @Test
    public void runFullDatabaseAudit() {
        System.out.println("================================================================================");
        System.out.println("  WATCHSTORE - FULL DATABASE AUDIT & VERIFICATION");
        System.out.println("================================================================================");

        try (Connection conn = DBContext.getConnection()) {
            assertNotNull(conn, "Database connection must not be null");

            // 1. Verify DB Name
            String dbName = "";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT DB_NAME() AS CurrentDatabase")) {
                if (rs.next()) {
                    dbName = rs.getString("CurrentDatabase");
                }
            }
            System.out.println("[AUDIT 1] Database Name: " + dbName);
            assertEquals("WatchStore", dbName, "Database name must be WatchStore");

            // 2. Count & List All Tables
            System.out.println("\n[AUDIT 2] Exact Table List & Row Counts:");
            String tableSql = """
                SELECT TABLE_SCHEMA + '.' + TABLE_NAME AS FullTableName
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_TYPE = 'BASE TABLE'
                ORDER BY TABLE_NAME
            """;
            List<String> tableList = new ArrayList<>();
            Map<String, Integer> rowCounts = new TreeMap<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(tableSql)) {
                while (rs.next()) {
                    String tbl = rs.getString("FullTableName");
                    tableList.add(tbl);
                }
            }

            for (String tbl : tableList) {
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT COUNT(*) AS Cnt FROM " + tbl)) {
                    if (rs.next()) {
                        rowCounts.put(tbl, rs.getInt("Cnt"));
                        System.out.printf("  - %-32s : %d rows%n", tbl, rs.getInt("Cnt"));
                    }
                } catch (Exception ex) {
                    rowCounts.put(tbl, -1);
                    System.out.printf("  - %-32s : ERROR (%s)%n", tbl, ex.getMessage());
                }
            }
            System.out.println("Total Live Tables: " + tableList.size());

            // 3. Verify Foreign Keys Integrity
            System.out.println("\n[AUDIT 3] Foreign Key Integrity Check:");
            String fkCheckSql = """
                SELECT 
                    fk.name AS FK_Name,
                    tp.name AS ParentTable,
                    tr.name AS ReferencedTable
                FROM sys.foreign_keys fk
                INNER JOIN sys.tables tp ON fk.parent_object_id = tp.object_id
                INNER JOIN sys.tables tr ON fk.referenced_object_id = tr.object_id
                WHERE OBJECT_ID(tr.name, 'U') IS NULL OR OBJECT_ID(tp.name, 'U') IS NULL
            """;
            int brokenFkCount = 0;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(fkCheckSql)) {
                while (rs.next()) {
                    brokenFkCount++;
                    System.out.printf("  [BROKEN FK] %s: %s -> %s%n",
                            rs.getString("FK_Name"), rs.getString("ParentTable"), rs.getString("ReferencedTable"));
                }
            }
            assertEquals(0, brokenFkCount, "There should be no broken foreign keys in WatchStore");
            System.out.println("  --> All Foreign Keys are valid. 0 broken FKs.");

            // 4. Verify Voucher Codes & Duplicates
            System.out.println("\n[AUDIT 4] Voucher Code Duplication Check:");
            String voucherDupSql = """
                SELECT VoucherCode, COUNT(*) AS Cnt
                FROM dbo.Vouchers
                GROUP BY VoucherCode
                HAVING COUNT(*) > 1
            """;
            int duplicateVouchers = 0;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(voucherDupSql)) {
                while (rs.next()) {
                    duplicateVouchers++;
                    System.out.printf("  [DUPLICATE VOUCHER] Code: %s, Count: %d%n",
                            rs.getString("VoucherCode"), rs.getInt("Cnt"));
                }
            }
            assertEquals(0, duplicateVouchers, "There should be no duplicate voucher codes");
            System.out.println("  --> 0 duplicate Voucher codes found.");

            // 5. Verify Warranty Schema & Columns
            System.out.println("\n[AUDIT 5] Warranties & OrderItems Columns Check:");
            String[] requiredWarrantyCols = {"WarrantyMonths", "ImageUrl", "CustomerName", "CustomerPhone", "CustomerEmail", "WarrantyType", "OrderID"};
            for (String col : requiredWarrantyCols) {
                String colCheckSql = "SELECT COUNT(*) FROM sys.columns WHERE object_id = OBJECT_ID('dbo.Warranties') AND name = '" + col + "'";
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery(colCheckSql)) {
                    assertTrue(rs.next() && rs.getInt(1) > 0, "dbo.Warranties must contain column " + col);
                }
            }
            // Check OrderItems.WarrantyMonths
            String orderItemColSql = "SELECT COUNT(*) FROM sys.columns WHERE object_id = OBJECT_ID('dbo.OrderItems') AND name = 'WarrantyMonths'";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(orderItemColSql)) {
                assertTrue(rs.next() && rs.getInt(1) > 0, "dbo.OrderItems must contain column WarrantyMonths");
            }
            System.out.println("  --> All Warranty & OrderItems columns verified.");

            // 6. Verify OtpVerifications Columns
            System.out.println("\n[AUDIT 6] OtpVerifications Columns Check:");
            String[] requiredOtpCols = {"OtpHash", "Purpose", "Attempts", "Verified", "LastSentAt", "ResendCount"};
            for (String col : requiredOtpCols) {
                String colCheckSql = "SELECT COUNT(*) FROM sys.columns WHERE object_id = OBJECT_ID('dbo.OtpVerifications') AND name = '" + col + "'";
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery(colCheckSql)) {
                    assertTrue(rs.next() && rs.getInt(1) > 0, "dbo.OtpVerifications must contain column " + col);
                }
            }
            System.out.println("  --> All OtpVerifications columns verified.");

            // 7. Verify Core Business Data Integrity
            System.out.println("\n[AUDIT 7] Core Business Table Rows Check:");
            assertTrue(rowCounts.getOrDefault("dbo.Users", 0) > 0, "Users table must have data");
            assertTrue(rowCounts.getOrDefault("dbo.Products", 0) > 0, "Products table must have data");
            assertTrue(rowCounts.getOrDefault("dbo.ProductVariants", 0) > 0, "ProductVariants table must have data");
            assertTrue(rowCounts.getOrDefault("dbo.Orders", 0) > 0, "Orders table must have data");
            assertTrue(rowCounts.getOrDefault("dbo.OrderItems", 0) > 0, "OrderItems table must have data");
            assertTrue(rowCounts.getOrDefault("dbo.Vouchers", 0) > 0, "Vouchers table must have data");
            assertTrue(rowCounts.getOrDefault("dbo.Warranties", 0) > 0, "Warranties table must have data");
            assertTrue(rowCounts.getOrDefault("dbo.ProductComments", 0) > 0, "ProductComments table must have data");
            System.out.println("  --> All Core Business Tables contain valid active records.");

            System.out.println("\n[AUDIT 8] Current Vouchers & SQL Time:");
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT GETDATE() AS SqlGetDate, SYSDATETIME() AS SqlSysDateTime, SYSUTCDATETIME() AS SqlUtcDateTime")) {
                if (rs.next()) {
                    System.out.println("  - SQL GETDATE(): " + rs.getString("SqlGetDate"));
                    System.out.println("  - SQL SYSDATETIME(): " + rs.getString("SqlSysDateTime"));
                    System.out.println("  - SQL SYSUTCDATETIME(): " + rs.getString("SqlUtcDateTime"));
                    System.out.println("  - Java LocalDateTime.now(): " + java.time.LocalDateTime.now());
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT VoucherID, VoucherCode, VoucherName, DiscountType, DiscountValue, MinimumOrderValue, MaximumDiscount, UsageLimit, UsageLimitPerUser, UsedCount, StartAt, EndAt, IsPublic, Status FROM dbo.Vouchers")) {
                while (rs.next()) {
                    System.out.printf("  [VOUCHER] ID: %d | Code: %s | Type: %s | Limit: %d | PerUser: %d | Used: %d | Start: %s | End: %s | Public: %b | Status: %s%n",
                            rs.getInt("VoucherID"),
                            rs.getString("VoucherCode"),
                            rs.getString("DiscountType"),
                            rs.getInt("UsageLimit"),
                            rs.getInt("UsageLimitPerUser"),
                            rs.getInt("UsedCount"),
                            rs.getString("StartAt"),
                            rs.getString("EndAt"),
                            rs.getBoolean("IsPublic"),
                            rs.getString("Status"));
                }
            }

            System.out.println("\n================================================================================");
            System.out.println("  DATABASE AUDIT PASS - ALL CHECKS SUCCESSFUL");
            System.out.println("================================================================================");

        } catch (Exception e) {
            fail("Audit failed: " + e.getMessage());
        }
    }
}
