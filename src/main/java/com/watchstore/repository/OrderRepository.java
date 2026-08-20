package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderRepository {

    static {
        try (Connection conn = DBContext.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = """
                IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[OrderHistory]') AND type in (N'U'))
                BEGIN
                    CREATE TABLE dbo.OrderHistory (
                        HistoryID           BIGINT IDENTITY(1,1) PRIMARY KEY,
                        OrderID             BIGINT NOT NULL,
                        ActionType          NVARCHAR(50) NOT NULL,
                        ActionDetail        NVARCHAR(1000) NOT NULL,
                        PerformedBy         NVARCHAR(150) NOT NULL,
                        CreatedAt           DATETIME2 NOT NULL DEFAULT SYSDATETIME()
                    );
                END
                """;
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM dbo.Orders ORDER BY OrderID DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Order findById(int id) {
        String sql = "SELECT * FROM dbo.Orders WHERE OrderID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Order findByCode(String code) {
        String sql = "SELECT * FROM dbo.Orders WHERE OrderCode = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Order> findByCustomerId(int customerId) {
        List<Order> list = new ArrayList<>();

        String sql = """
                SELECT *
                FROM dbo.Orders
                WHERE CustomerID = ?
                ORDER BY OrderID DESC
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Map<String, Object>> getOrderItems(int orderId) {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = """
                SELECT
                    oi.OrderItemID,
                    oi.OrderID,
                    oi.VariantID,
                    oi.ProductName,
                    oi.VariantName,
                    oi.SKU,
                    oi.ImageUrl,
                    oi.Quantity,
                    oi.UnitPrice,
                    oi.DiscountAmount,
                    oi.LineTotal,
                    ISNULL(oi.WarrantyMonths, 12) AS WarrantyMonths,
                    pv.ProductID,
                    o.CreatedAt AS OrderDate,
                    DATEADD(month, ISNULL(oi.WarrantyMonths, 12), o.CreatedAt) AS WarrantyEndDate
                FROM dbo.OrderItems oi
                JOIN dbo.Orders o ON o.OrderID = oi.OrderID
                LEFT JOIN dbo.ProductVariants pv ON oi.VariantID = pv.VariantID
                WHERE oi.OrderID = ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                long now = System.currentTimeMillis();
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("orderItemId", rs.getLong("OrderItemID"));
                    item.put("orderId", rs.getLong("OrderID"));
                    item.put("variantId", rs.getInt("VariantID"));
                    item.put("productId", rs.getInt("ProductID"));
                    item.put("productName", rs.getString("ProductName"));
                    item.put("variantName", rs.getString("VariantName"));
                    item.put("sku", rs.getString("SKU"));
                    item.put("imageUrl", rs.getString("ImageUrl"));
                    item.put("quantity", rs.getInt("Quantity"));
                    item.put("price", rs.getBigDecimal("UnitPrice"));
                    item.put("discountAmount", rs.getBigDecimal("DiscountAmount"));
                    item.put("lineTotal", rs.getBigDecimal("LineTotal"));

                    int months = rs.getInt("WarrantyMonths");
                    item.put("warrantyMonths", months > 0 ? months : 12);

                    Timestamp orderDate = rs.getTimestamp("OrderDate");
                    Timestamp endDate = rs.getTimestamp("WarrantyEndDate");
                    item.put("warrantyStartDate", orderDate);
                    item.put("warrantyEndDate", endDate);

                    boolean isExpired = endDate != null && now > endDate.getTime();
                    long remainingDays = 0;
                    if (endDate != null && !isExpired) {
                        remainingDays = (endDate.getTime() - now) / (1000L * 60 * 60 * 24);
                    }
                    item.put("isWarrantyExpired", isExpired);
                    item.put("remainingWarrantyDays", remainingDays);

                    list.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public long createFromCart(int userId, int addressId, String voucherCode, String payment, String note) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Khách hàng cần đăng nhập để đặt hàng.");
        }
        if (addressId <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn địa chỉ nhận hàng.");
        }

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            // 1. Lấy danh sách sản phẩm trong giỏ hàng
            String getCartItemsSql = """
                SELECT ci.VariantID, ci.Quantity, pv.SalePrice, pv.SKU, pv.VariantName, p.ProductID, p.ProductName,
                       ISNULL(p.WarrantyMonths, 12) AS WarrantyMonths,
                       (SELECT TOP 1 ImageUrl FROM dbo.ProductImages WHERE ProductID = p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC) AS ImageUrl,
                       ISNULL((SELECT SUM(AvailableQuantity) FROM dbo.InventoryBalances WHERE VariantID = pv.VariantID), 999) AS AvailableStock
                FROM dbo.CartItems ci
                JOIN dbo.Carts c ON c.CartID = ci.CartID AND c.Status = 'ACTIVE'
                JOIN dbo.ProductVariants pv ON pv.VariantID = ci.VariantID AND pv.Status = 'ACTIVE'
                JOIN dbo.Products p ON p.ProductID = pv.ProductID AND p.Status = 'ACTIVE'
                WHERE c.UserID = ?
                """;

            List<Map<String, Object>> cartItems = new ArrayList<>();
            BigDecimal subtotal = BigDecimal.ZERO;

            try (PreparedStatement ps = conn.prepareStatement(getCartItemsSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> item = new HashMap<>();
                        int variantId = rs.getInt("VariantID");
                        int quantity = rs.getInt("Quantity");
                        BigDecimal salePrice = rs.getBigDecimal("SalePrice");
                        if (salePrice == null) salePrice = BigDecimal.ZERO;
                        int availableStock = rs.getInt("AvailableStock");

                        if (quantity > availableStock && availableStock > 0) {
                            throw new SQLException("Sản phẩm '" + rs.getString("ProductName") + "' không đủ số lượng tồn kho.");
                        }

                        item.put("variantId", variantId);
                        item.put("productId", rs.getInt("ProductID"));
                        item.put("quantity", quantity);
                        item.put("unitPrice", salePrice);
                        item.put("sku", rs.getString("SKU"));
                        item.put("variantName", rs.getString("VariantName"));
                        item.put("productName", rs.getString("ProductName"));
                        item.put("imageUrl", rs.getString("ImageUrl"));
                        int wMonths = rs.getInt("WarrantyMonths");
                        item.put("warrantyMonths", wMonths > 0 ? wMonths : 12);

                        BigDecimal lineTotal = salePrice.multiply(BigDecimal.valueOf(quantity));
                        subtotal = subtotal.add(lineTotal);
                        cartItems.add(item);
                    }
                }
            }

            if (cartItems.isEmpty()) {
                throw new SQLException("Giỏ hàng của bạn đang trống.");
            }

            // 2. Kiểm tra và lấy thông tin địa chỉ nhận hàng
            String getAddressSql = "SELECT RecipientName, RecipientPhone, Province, District, Ward, AddressLine FROM dbo.UserAddresses WHERE AddressID = ? AND UserID = ?";
            String recipientName = "";
            String recipientPhone = "";
            String fullAddress = "";

            try (PreparedStatement ps = conn.prepareStatement(getAddressSql)) {
                ps.setInt(1, addressId);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Địa chỉ giao hàng không hợp lệ hoặc không thuộc tài khoản của bạn.");
                    }
                    recipientName = rs.getString("RecipientName");
                    recipientPhone = rs.getString("RecipientPhone");
                    String province = rs.getString("Province");
                    String district = rs.getString("District");
                    String ward = rs.getString("Ward");
                    String line = rs.getString("AddressLine");
                    fullAddress = line + ", " + ward + ", " + district + ", " + province;
                }
            }

            // 3. Xử lý Voucher nếu có
            Integer voucherId = null;
            BigDecimal discountAmount = BigDecimal.ZERO;

            if (voucherCode != null && !voucherCode.trim().isEmpty()) {
                String checkVoucherSql = """
                    SELECT VoucherID, VoucherCode, DiscountType, DiscountValue, MinimumOrderValue, MaximumDiscount,
                           UsageLimit, UsageLimitPerUser, UsedCount, StartAt, EndAt, Status
                    FROM dbo.Vouchers
                    WHERE LOWER(VoucherCode) = LOWER(?) AND Status = 'ACTIVE'
                    """;

                try (PreparedStatement ps = conn.prepareStatement(checkVoucherSql)) {
                    ps.setString(1, voucherCode.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Mã giảm giá không tồn tại hoặc đã hết hiệu lực.");
                        }

                        voucherId = rs.getInt("VoucherID");
                        String discountType = rs.getString("DiscountType");
                        BigDecimal discountValue = rs.getBigDecimal("DiscountValue");
                        BigDecimal minOrder = rs.getBigDecimal("MinimumOrderValue");
                        BigDecimal maxDiscount = rs.getBigDecimal("MaximumDiscount");
                        int usageLimit = rs.getInt("UsageLimit");
                        int usageLimitPerUser = rs.getInt("UsageLimitPerUser");
                        int usedCount = rs.getInt("UsedCount");
                        Timestamp startAt = rs.getTimestamp("StartAt");
                        Timestamp endAt = rs.getTimestamp("EndAt");

                        long now = System.currentTimeMillis();
                        if (startAt != null && now < startAt.getTime()) {
                            throw new SQLException("Mã giảm giá chưa đến thời gian áp dụng.");
                        }
                        if (endAt != null && now > endAt.getTime()) {
                            throw new SQLException("Mã giảm giá đã hết hạn sử dụng.");
                        }
                        if (minOrder != null && subtotal.compareTo(minOrder) < 0) {
                            throw new SQLException("Đơn hàng chưa đạt giá trị tối thiểu " + String.format("%,.0f", minOrder) + "đ để dùng mã này.");
                        }
                        if (usageLimit > 0 && usedCount >= usageLimit) {
                            throw new SQLException("Mã giảm giá đã hết lượt sử dụng.");
                        }

                        // Kiểm tra lượt dùng của User
                        String countUserUsageSql = "SELECT COUNT(*) FROM dbo.VoucherUsages WHERE VoucherID = ? AND UserID = ?";
                        try (PreparedStatement psUsage = conn.prepareStatement(countUserUsageSql)) {
                            psUsage.setInt(1, voucherId);
                            psUsage.setInt(2, userId);
                            try (ResultSet rsUsage = psUsage.executeQuery()) {
                                if (rsUsage.next() && rsUsage.getInt(1) >= usageLimitPerUser) {
                                    throw new SQLException("Bạn đã sử dụng hết số lượt cho phép của mã giảm giá này.");
                                }
                            }
                        }

                        // Tính số tiền giảm
                        if ("PERCENT".equalsIgnoreCase(discountType)) {
                            discountAmount = subtotal.multiply(discountValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                            if (maxDiscount != null && maxDiscount.compareTo(BigDecimal.ZERO) > 0 && discountAmount.compareTo(maxDiscount) > 0) {
                                discountAmount = maxDiscount;
                            }
                        } else {
                            discountAmount = discountValue != null ? discountValue : BigDecimal.ZERO;
                        }

                        if (discountAmount.compareTo(subtotal) > 0) {
                            discountAmount = subtotal;
                        }
                    }
                }
            }

            // 4. Tính phí vận chuyển & Tổng tiền
            BigDecimal shippingFee = (subtotal.compareTo(BigDecimal.ZERO) > 0 && subtotal.compareTo(new BigDecimal("1000000")) < 0)
                    ? new BigDecimal("30000")
                    : BigDecimal.ZERO;

            BigDecimal totalAmount = subtotal.subtract(discountAmount).add(shippingFee);
            if (totalAmount.compareTo(BigDecimal.ZERO) < 0) totalAmount = BigDecimal.ZERO;

            // 5. Sinh OrderCode duy nhất
            String orderCode = "WS" + (System.currentTimeMillis() % 1000000) + String.format("%02d", new java.util.Random().nextInt(100));

            // 6. Tạo Orders
            String insertOrderSql = """
                INSERT INTO dbo.Orders
                (OrderCode, CustomerID, VoucherID, RecipientName, RecipientPhone, ShippingAddress, CustomerNote,
                 OrderStatus, PaymentStatus, SubtotalAmount, DiscountAmount, ShippingFee, TotalAmount, CreatedAt, UpdatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, ?, ?, ?, ?, SYSDATETIME(), SYSDATETIME())
                """;

            long orderId;
            try (PreparedStatement ps = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, orderCode);
                ps.setInt(2, userId);
                if (voucherId != null) {
                    ps.setInt(3, voucherId);
                } else {
                    ps.setNull(3, Types.INTEGER);
                }
                ps.setString(4, recipientName);
                ps.setString(5, recipientPhone);
                ps.setString(6, fullAddress);
                if (note != null && !note.isBlank()) {
                    ps.setString(7, note.trim());
                } else {
                    ps.setNull(7, Types.NVARCHAR);
                }
                ps.setString(8, "COD".equalsIgnoreCase(payment) ? "UNPAID" : ("BANK_TRANSFER".equalsIgnoreCase(payment) ? "WAITING_PAYMENT" : "UNPAID"));
                ps.setBigDecimal(9, subtotal);
                ps.setBigDecimal(10, discountAmount);
                ps.setBigDecimal(11, shippingFee);
                ps.setBigDecimal(12, totalAmount);

                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        orderId = keys.getLong(1);
                    } else {
                        throw new SQLException("Không thể tạo đơn hàng.");
                    }
                }
            }

            // 7. Tạo OrderItems
            String insertOrderItemSql = """
                INSERT INTO dbo.OrderItems (OrderID, VariantID, ProductName, VariantName, SKU, ImageUrl, UnitPrice, Quantity, DiscountAmount, WarrantyMonths)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, ?)
                """;

            try (PreparedStatement ps = conn.prepareStatement(insertOrderItemSql)) {
                for (Map<String, Object> item : cartItems) {
                    ps.setLong(1, orderId);
                    ps.setInt(2, (int) item.get("variantId"));
                    ps.setString(3, (String) item.get("productName"));
                    ps.setString(4, (String) item.get("variantName"));
                    ps.setString(5, (String) item.get("sku"));
                    String img = (String) item.get("imageUrl");
                    if (img != null && !img.isBlank()) {
                        ps.setString(6, img);
                    } else {
                        ps.setNull(6, Types.NVARCHAR);
                    }
                    ps.setBigDecimal(7, (BigDecimal) item.get("unitPrice"));
                    ps.setInt(8, (int) item.get("quantity"));
                    ps.setInt(9, (int) item.get("warrantyMonths"));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 8. Ghi VoucherUsage & tăng UsedCount
            if (voucherId != null) {
                String insertUsageSql = "INSERT INTO dbo.VoucherUsages (VoucherID, UserID, OrderID, DiscountAmount, UsedAt) VALUES (?, ?, ?, ?, SYSDATETIME())";
                try (PreparedStatement ps = conn.prepareStatement(insertUsageSql)) {
                    ps.setInt(1, voucherId);
                    ps.setInt(2, userId);
                    ps.setLong(3, orderId);
                    ps.setBigDecimal(4, discountAmount);
                    ps.executeUpdate();
                }

                String updateVoucherCountSql = "UPDATE dbo.Vouchers SET UsedCount = UsedCount + 1 WHERE VoucherID = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateVoucherCountSql)) {
                    ps.setInt(1, voucherId);
                    ps.executeUpdate();
                }
            }

            // 9. Xóa CartItems của user
            String deleteCartSql = """
                DELETE ci
                FROM dbo.CartItems ci
                JOIN dbo.Carts c ON c.CartID = ci.CartID
                WHERE c.UserID = ? AND c.Status = 'ACTIVE'
                """;
            try (PreparedStatement ps = conn.prepareStatement(deleteCartSql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 10. Ghi OrderHistory
            String logHistorySql = "INSERT INTO dbo.OrderHistory (OrderID, ActionType, ActionDetail, PerformedBy, CreatedAt) VALUES (?, 'CREATE_ORDER', N'Khách hàng đặt hàng trực tuyến', N'Khách hàng', SYSDATETIME())";
            try (PreparedStatement ps = conn.prepareStatement(logHistorySql)) {
                ps.setLong(1, orderId);
                ps.executeUpdate();
            }

            conn.commit();
            return orderId;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public void cancel(long orderId, int userId, String reason) throws SQLException {
        if (orderId <= 0 || userId <= 0) {
            throw new IllegalArgumentException("Thông tin đơn hàng không hợp lệ.");
        }
        if (reason == null || reason.trim().length() < 3) {
            throw new IllegalArgumentException("Vui lòng nhập lý do hủy đơn từ 3 ký tự trở lên.");
        }

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            String checkOrderSql = "SELECT OrderStatus, VoucherID FROM dbo.Orders WHERE OrderID = ? AND CustomerID = ?";
            Integer voucherId = null;

            try (PreparedStatement ps = conn.prepareStatement(checkOrderSql)) {
                ps.setLong(1, orderId);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Đơn hàng không tồn tại hoặc không thuộc quyền sở hữu của bạn.");
                    }
                    String status = rs.getString("OrderStatus");
                    if (!"PENDING".equalsIgnoreCase(status)) {
                        throw new SQLException("Chỉ có thể hủy đơn hàng khi đơn đang ở trạng thái Chờ xác nhận (PENDING).");
                    }
                    int vId = rs.getInt("VoucherID");
                    if (!rs.wasNull()) {
                        voucherId = vId;
                    }
                }
            }

            // Cập nhật trạng thái đơn sang CANCELLED
            String updateOrderSql = "UPDATE dbo.Orders SET OrderStatus = 'CANCELLED', CancelledAt = SYSDATETIME(), UpdatedAt = SYSDATETIME() WHERE OrderID = ? AND CustomerID = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateOrderSql)) {
                ps.setLong(1, orderId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

            // Hoàn lại voucher nếu đã dùng
            if (voucherId != null) {
                String revertVoucherSql = "UPDATE dbo.Vouchers SET UsedCount = CASE WHEN UsedCount > 0 THEN UsedCount - 1 ELSE 0 END WHERE VoucherID = ?";
                try (PreparedStatement ps = conn.prepareStatement(revertVoucherSql)) {
                    ps.setInt(1, voucherId);
                    ps.executeUpdate();
                }

                String deleteUsageSql = "DELETE FROM dbo.VoucherUsages WHERE OrderID = ? AND UserID = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteUsageSql)) {
                    ps.setLong(1, orderId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }
            }

            // Ghi OrderHistory
            String logHistorySql = "INSERT INTO dbo.OrderHistory (OrderID, ActionType, ActionDetail, PerformedBy, CreatedAt) VALUES (?, 'CANCEL_ORDER', ?, N'Khách hàng', SYSDATETIME())";
            try (PreparedStatement ps = conn.prepareStatement(logHistorySql)) {
                ps.setLong(1, orderId);
                ps.setString(2, "Khách hàng hủy đơn: " + reason.trim());
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public List<Order> search(String keyword, String status) {
        return search(keyword, status, null, null);
    }

    public List<Order> search(String keyword, String status, String fromDate, String toDate) {
        return search(keyword, status, fromDate, toDate, null, null);
    }

    public List<Order> search(String keyword, String status, String fromDate, String toDate, BigDecimal minAmount, BigDecimal maxAmount) {
        List<Order> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM dbo.Orders WHERE 1=1 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(RecipientName) LIKE ? OR RecipientPhone LIKE ? OR LOWER(OrderCode) LIKE ?)");
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND OrderStatus = ? ");
        }

        if (fromDate != null && !fromDate.trim().isEmpty()) {
            sql.append(" AND CreatedAt >= ? ");
        }

        if (toDate != null && !toDate.trim().isEmpty()) {
            sql.append(" AND CreatedAt <= ? ");
        }

        if (minAmount != null) {
            sql.append(" AND TotalAmount >= ? ");
        }

        if (maxAmount != null) {
            sql.append(" AND TotalAmount <= ? ");
        }

        sql.append(" ORDER BY OrderID DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String keyPattern = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(paramIndex++, keyPattern);
                ps.setString(paramIndex++, keyPattern);
                ps.setString(paramIndex++, keyPattern);
            }

            if (status != null && !status.trim().isEmpty()) {
                ps.setString(paramIndex++, status.trim());
            }

            if (fromDate != null && !fromDate.trim().isEmpty()) {
                ps.setString(paramIndex++, fromDate.trim() + " 00:00:00");
            }

            if (toDate != null && !toDate.trim().isEmpty()) {
                ps.setString(paramIndex++, toDate.trim() + " 23:59:59");
            }

            if (minAmount != null) {
                ps.setBigDecimal(paramIndex++, minAmount);
            }

            if (maxAmount != null) {
                ps.setBigDecimal(paramIndex++, maxAmount);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void add(Order order) {
        String sql = """
                INSERT INTO dbo.Orders
                (OrderCode, CustomerID, RecipientName, RecipientPhone, ShippingAddress, SubtotalAmount, DiscountAmount, ShippingFee, TotalAmount, OrderStatus, PaymentStatus, CreatedAt, UpdatedAt)
                VALUES (?, ?, ?, ?, ?, ?, 0, 0, ?, ?, ?, SYSDATETIME(), SYSDATETIME())
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, order.getCode());
            ps.setInt(2, order.getUserId() > 0 ? order.getUserId() : 4);
            ps.setString(3, order.getCustomerName() != null ? order.getCustomerName() : "Khách hàng WatchStore");
            ps.setString(4, order.getPhone() != null ? order.getPhone() : "0988 686 868");
            ps.setString(5, order.getShippingAddress() != null ? order.getShippingAddress() : "Hà Nội");

            BigDecimal totalPrice = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
            ps.setBigDecimal(6, totalPrice);
            ps.setBigDecimal(7, totalPrice);
            ps.setString(8, order.getStatus() != null ? order.getStatus().name() : "PENDING");
            ps.setString(9, order.getPaymentStatus() != null ? order.getPaymentStatus() : "UNPAID");

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE dbo.Orders SET OrderStatus = ?, UpdatedAt = SYSDATETIME() WHERE OrderID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePaymentStatus(int id, String paymentStatus) {
        String sql = "UPDATE dbo.Orders SET PaymentStatus = ?, UpdatedAt = SYSDATETIME() WHERE OrderID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Order order) {
        String sql = """
                UPDATE dbo.Orders
                SET RecipientName = ?,
                    RecipientPhone = ?,
                    ShippingAddress = ?,
                    TotalAmount = ?,
                    OrderStatus = ?,
                    PaymentStatus = ?,
                    UpdatedAt = SYSDATETIME()
                WHERE OrderID = ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, order.getCustomerName());
            ps.setString(2, order.getPhone());
            ps.setString(3, order.getShippingAddress());
            BigDecimal totalPrice = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
            ps.setBigDecimal(4, totalPrice);
            ps.setString(5, order.getStatus() != null ? order.getStatus().name() : "PENDING");
            ps.setString(6, order.getPaymentStatus() != null ? order.getPaymentStatus() : "UNPAID");
            ps.setInt(7, order.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String deleteItemsSql = "DELETE FROM dbo.OrderItems WHERE OrderID = ?";
        String deleteOrderSql = "DELETE FROM dbo.Orders WHERE OrderID = ?";

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psItems = conn.prepareStatement(deleteItemsSql);
                 PreparedStatement psOrder = conn.prepareStatement(deleteOrderSql)) {

                psItems.setInt(1, id);
                psItems.executeUpdate();

                psOrder.setInt(1, id);
                int affectedRows = psOrder.executeUpdate();

                conn.commit();
                return affectedRows > 0;
            } catch (SQLException e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("OrderID"));
        order.setCode(rs.getString("OrderCode"));
        order.setUserId(rs.getInt("CustomerID"));
        order.setCustomerName(rs.getString("RecipientName"));
        order.setPhone(rs.getString("RecipientPhone"));
        order.setShippingAddress(rs.getString("ShippingAddress"));

        BigDecimal totalAmount = rs.getBigDecimal("TotalAmount");
        order.setTotalPrice(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        order.setStatus(rs.getString("OrderStatus"));
        order.setPaymentStatus(rs.getString("PaymentStatus"));

        BigDecimal discountAmount = rs.getBigDecimal("DiscountAmount");
        order.setDiscountAmount(discountAmount != null ? discountAmount : BigDecimal.ZERO);

        Timestamp time = rs.getTimestamp("CreatedAt");
        if (time != null) {
            order.setCreatedAt(new java.util.Date(time.getTime()));
        }

        return order;
    }

    public boolean cancelOrderAndRestoreStock(int orderId) {
        String updateOrderSql = """
                UPDATE dbo.Orders
                SET OrderStatus = 'CANCELLED',
                    CancelledAt = SYSDATETIME(),
                    UpdatedAt = SYSDATETIME()
                WHERE OrderID = ? AND OrderStatus != 'CANCELLED'
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateOrderSql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void logHistory(long orderId, String actionType, String detail, String performedBy) {
        String sql = "INSERT INTO dbo.OrderHistory (OrderID, ActionType, ActionDetail, PerformedBy, CreatedAt) VALUES (?, ?, ?, ?, SYSDATETIME())";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setString(2, actionType);
            ps.setString(3, detail);
            ps.setString(4, performedBy);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getHistory(long orderId) {
        List<Map<String, Object>> historyList = new ArrayList<>();
        String sql = "SELECT HistoryID, ActionType, ActionDetail, PerformedBy, CreatedAt FROM dbo.OrderHistory WHERE OrderID = ? ORDER BY HistoryID DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getLong("HistoryID"));
                    map.put("actionType", rs.getString("ActionType"));
                    map.put("detail", rs.getString("ActionDetail"));
                    map.put("performedBy", rs.getString("PerformedBy"));
                    map.put("createdAt", rs.getTimestamp("CreatedAt"));
                    historyList.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historyList;
    }
}
