package com.watchstore.repository;

import com.watchstore.config.DBContext;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class StatisticRepositoryImpl implements StatisticRepository {

    private Connection getConnection() throws SQLException {
        return DBContext.getConnection();
    }

    @Override
    public BigDecimal getTotalRevenue() {
        String sql = "SELECT ISNULL(SUM(TotalAmount), 0) FROM Orders WHERE OrderStatus NOT IN ('CANCELLED', 'RETURNED')";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public int getTotalOrdersCount() {
        String sql = "SELECT COUNT(*) FROM Orders";
        return queryCount(sql);
    }

    @Override
    public int getTotalProductsCount() {
        String sql = "SELECT COUNT(*) FROM Products";
        return queryCount(sql);
    }

    @Override
    public int getTotalCustomersCount() {
        String sql = "SELECT COUNT(*) FROM Users";
        return queryCount(sql);
    }

    @Override
    public int getTotalBrandsCount() {
        String sql = "SELECT COUNT(*) FROM Brands";
        return queryCount(sql);
    }

    @Override
    public int getTotalCategoriesCount() {
        String sql = "SELECT COUNT(*) FROM Categories";
        return queryCount(sql);
    }

    @Override
    public int getTotalVouchersCount() {
        String sql = "SELECT COUNT(*) FROM Vouchers";
        return queryCount(sql);
    }

    @Override
    public int getLowStockCount() {
        return 0;
    }

    private int queryCount(String sql) {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Map<String, Integer> getOrderStatusCounts() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT OrderStatus, COUNT(*) AS Qty FROM Orders GROUP BY OrderStatus";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("OrderStatus"), rs.getInt("Qty"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map<String, Integer> getProductsByBrandCounts() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = """
            SELECT b.BrandName, COUNT(p.ProductID) AS Qty
            FROM Brands b
            LEFT JOIN Products p ON b.BrandID = p.BrandID
            GROUP BY b.BrandName
            ORDER BY Qty DESC
            """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("BrandName"), rs.getInt("Qty"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map<String, Integer> getProductsByCategoryCounts() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = """
            SELECT c.CategoryName, COUNT(p.ProductID) AS Qty
            FROM Categories c
            LEFT JOIN Products p ON c.CategoryID = p.CategoryID
            GROUP BY c.CategoryName
            ORDER BY Qty DESC
            """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("CategoryName"), rs.getInt("Qty"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public List<Map<String, Object>> getDailySalesTrend() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT TOP 10 SalesDate, TotalOrders, GrossRevenue, NetRevenue FROM vw_DashboardSalesDaily ORDER BY SalesDate DESC";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("salesDate", rs.getDate("SalesDate"));
                row.put("totalOrders", rs.getInt("TotalOrders"));
                row.put("grossRevenue", rs.getBigDecimal("GrossRevenue"));
                row.put("netRevenue", rs.getBigDecimal("NetRevenue"));
                list.add(row);
            }
        } catch (Exception e) {
            // Fallback query directly from Orders
            String fallback = """
                SELECT TOP 10 CAST(CreatedAt AS DATE) AS SalesDate, COUNT(OrderID) AS TotalOrders, SUM(TotalAmount) AS NetRevenue
                FROM Orders
                WHERE OrderStatus NOT IN ('CANCELLED', 'RETURNED')
                GROUP BY CAST(CreatedAt AS DATE)
                ORDER BY SalesDate DESC
                """;
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(fallback);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("salesDate", rs.getDate("SalesDate"));
                    row.put("totalOrders", rs.getInt("TotalOrders"));
                    row.put("netRevenue", rs.getBigDecimal("NetRevenue"));
                    list.add(row);
                }
            } catch (Exception ignored) {}
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getTopSellingProducts() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT TOP 10 ProductID, ProductName, SKU, VariantName, TotalQuantitySold, TotalRevenue FROM vw_TopSellingProducts";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("productId", rs.getInt("ProductID"));
                row.put("productName", rs.getString("ProductName"));
                row.put("sku", rs.getString("SKU"));
                row.put("variantName", rs.getString("VariantName"));
                row.put("quantitySold", rs.getInt("TotalQuantitySold"));
                row.put("revenue", rs.getBigDecimal("TotalRevenue"));
                list.add(row);
            }
        } catch (Exception e) {
            // Safe empty list if no sales yet
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getTopCustomers() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT TOP 10 CustomerID, FullName, Email, Phone, TotalOrdersCount, TotalSpentAmount, LastOrderDate FROM vw_CustomerSummary ORDER BY TotalSpentAmount DESC";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("customerId", rs.getInt("CustomerID"));
                row.put("fullName", rs.getString("FullName"));
                row.put("email", rs.getString("Email"));
                row.put("phone", rs.getString("Phone"));
                row.put("ordersCount", rs.getInt("TotalOrdersCount"));
                row.put("spentAmount", rs.getBigDecimal("TotalSpentAmount"));
                row.put("lastOrderDate", rs.getTimestamp("LastOrderDate"));
                list.add(row);
            }
        } catch (Exception e) {
            // Safe fallback directly from Users table
            String fallback = "SELECT TOP 10 UserID AS CustomerID, FullName, Email, Phone, Status FROM Users ORDER BY UserID DESC";
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(fallback);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("customerId", rs.getInt("CustomerID"));
                    row.put("fullName", rs.getString("FullName"));
                    row.put("email", rs.getString("Email"));
                    row.put("phone", rs.getString("Phone"));
                    row.put("ordersCount", 0);
                    row.put("spentAmount", BigDecimal.ZERO);
                    list.add(row);
                }
            } catch (Exception ignored) {}
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getLowStockItems() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT TOP 10 ProductID, ProductName, SKU, VariantName, QuantityOnHand, AvailableQuantity, StockStatus FROM vw_LowStock";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("productId", rs.getInt("ProductID"));
                row.put("productName", rs.getString("ProductName"));
                row.put("sku", rs.getString("SKU"));
                row.put("variantName", rs.getString("VariantName"));
                row.put("onHand", rs.getInt("QuantityOnHand"));
                row.put("available", rs.getInt("AvailableQuantity"));
                row.put("status", rs.getString("StockStatus"));
                list.add(row);
            }
        } catch (Exception e) {
            // Safe empty list if view unaccessible
        }
        return list;
    }

    @Override
    public int getPendingOrdersCount() {
        String sql = "SELECT COUNT(*) FROM Orders WHERE OrderStatus = 'PENDING'";
        return queryCount(sql);
    }

    @Override
    public int getActiveProductsCount() {
        String sql = "SELECT COUNT(*) FROM Products WHERE Status = 'ACTIVE'";
        return queryCount(sql);
    }

    @Override
    public int getActiveCustomersCount() {
        String sql = "SELECT COUNT(*) FROM Users WHERE Status = 'ACTIVE'";
        return queryCount(sql);
    }

    @Override
    public int getExpiringVouchersCount() {
        String sql = "SELECT COUNT(*) FROM Vouchers WHERE Status = 'ACTIVE' AND EndAt >= SYSDATETIME() AND EndAt <= DATEADD(day, 7, SYSDATETIME())";
        int count = queryCount(sql);
        if (count == 0) {
            String fallbackSql = "SELECT COUNT(*) FROM Vouchers WHERE Status = 'ACTIVE'";
            return queryCount(fallbackSql);
        }
        return count;
    }

    @Override
    public List<Map<String, Object>> getLast7DaysSales() {
        Map<java.time.LocalDate, Map<String, Object>> dayMap = new LinkedHashMap<>();
        java.time.LocalDate anchorDate = java.time.LocalDate.now();

        String maxDateSql = "SELECT MAX(CAST(CreatedAt AS DATE)) FROM Orders WHERE OrderStatus NOT IN ('CANCELLED', 'RETURNED')";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(maxDateSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                java.sql.Date maxSqlDate = rs.getDate(1);
                if (maxSqlDate != null) {
                    java.time.LocalDate maxDate = maxSqlDate.toLocalDate();
                    if (maxDate.isAfter(anchorDate)) {
                        anchorDate = maxDate;
                    }
                }
            }
        } catch (Exception ignored) {}

        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate date = anchorDate.minusDays(i);
            Map<String, Object> item = new HashMap<>();
            item.put("rawDate", date);
            item.put("dateLabel", date.format(fmt));
            item.put("revenue", BigDecimal.ZERO);
            item.put("ordersCount", 0);
            dayMap.put(date, item);
        }

        String sql = "SELECT CAST(CreatedAt AS DATE) AS SalesDate, COUNT(*) AS OrdersCount, ISNULL(SUM(TotalAmount), 0) AS NetRevenue " +
                     "FROM Orders " +
                     "WHERE OrderStatus NOT IN ('CANCELLED', 'RETURNED') " +
                     "  AND CreatedAt >= CAST(? AS DATE) " +
                     "GROUP BY CAST(CreatedAt AS DATE)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(anchorDate.minusDays(6)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("SalesDate");
                    if (sqlDate != null) {
                        java.time.LocalDate date = sqlDate.toLocalDate();
                        if (dayMap.containsKey(date)) {
                            Map<String, Object> item = dayMap.get(date);
                            BigDecimal netRev = rs.getBigDecimal("NetRevenue");
                            item.put("revenue", netRev != null ? netRev : BigDecimal.ZERO);
                            item.put("ordersCount", rs.getInt("OrdersCount"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        BigDecimal maxRevenue = BigDecimal.ZERO;
        for (Map<String, Object> item : dayMap.values()) {
            BigDecimal rev = (BigDecimal) item.get("revenue");
            if (rev != null && rev.compareTo(maxRevenue) > 0) {
                maxRevenue = rev;
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : dayMap.values()) {
            BigDecimal rev = (BigDecimal) item.get("revenue");
            int heightPercent = 14;
            if (maxRevenue.compareTo(BigDecimal.ZERO) > 0 && rev != null && rev.compareTo(BigDecimal.ZERO) > 0) {
                double ratio = rev.doubleValue() / maxRevenue.doubleValue();
                heightPercent = (int) Math.round(15 + ratio * 80);
            }
            item.put("heightPercent", heightPercent);
            item.put("isMax", maxRevenue.compareTo(BigDecimal.ZERO) > 0 && rev != null && rev.compareTo(maxRevenue) == 0);
            result.add(item);
        }

        return result;
    }

    @Override
    public List<com.watchstore.model.Order> getRecentOrders(int limit) {
        List<com.watchstore.model.Order> list = new ArrayList<>();
        int fetchLimit = limit > 0 ? limit : 8;
        String sql = "SELECT TOP " + fetchLimit + " o.OrderCode, ISNULL(NULLIF(o.RecipientName, ''), u.FullName) AS CustomerName, " +
                     "o.CreatedAt, o.TotalAmount, o.OrderStatus " +
                     "FROM Orders o " +
                     "LEFT JOIN Users u ON o.CustomerID = u.UserID " +
                     "ORDER BY o.CreatedAt DESC, o.OrderID DESC";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String code = rs.getString("OrderCode");
                String customerName = rs.getString("CustomerName");
                if (customerName == null || customerName.isBlank()) {
                    customerName = "Khách hàng";
                }
                Timestamp ts = rs.getTimestamp("CreatedAt");
                java.time.LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : java.time.LocalDateTime.now();
                BigDecimal total = rs.getBigDecimal("TotalAmount");
                if (total == null) total = BigDecimal.ZERO;
                String statusStr = rs.getString("OrderStatus");
                
                com.watchstore.enums.OrderStatus statusEnum;
                try {
                    statusEnum = com.watchstore.enums.OrderStatus.valueOf(statusStr);
                } catch (Exception ex) {
                    if ("PACKING".equalsIgnoreCase(statusStr)) {
                        statusEnum = com.watchstore.enums.OrderStatus.SHIPPING;
                    } else if ("DELIVERED".equalsIgnoreCase(statusStr)) {
                        statusEnum = com.watchstore.enums.OrderStatus.COMPLETED;
                    } else {
                        statusEnum = com.watchstore.enums.OrderStatus.PENDING;
                    }
                }
                
                list.add(new com.watchstore.model.Order(code, customerName, createdAt, total, statusEnum));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}

