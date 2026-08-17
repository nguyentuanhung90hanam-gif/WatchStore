package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Order;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderRepository {

    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM Orders ORDER BY OrderID DESC";

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
        String sql = "SELECT * FROM Orders WHERE OrderID = ?";

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
        String sql = "SELECT * FROM Orders WHERE OrderCode = ?";

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
                FROM Orders
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
                    oi.VariantID,
                    pv.ProductID,
                    pv.VariantName,
                    p.ProductName,
                    pv.SKU,
                    oi.Quantity,
                    oi.UnitPrice,
                    (oi.Quantity * oi.UnitPrice) AS LineTotal
                FROM OrderItems oi
                JOIN ProductVariants pv
                    ON oi.VariantID = pv.VariantID
                JOIN Products p
                    ON pv.ProductID = p.ProductID
                WHERE oi.OrderID = ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();

                    item.put("variantId", rs.getInt("VariantID"));
                    item.put("productId", rs.getInt("ProductID"));
                    item.put("productName", rs.getString("ProductName"));
                    item.put("variantName", rs.getString("VariantName"));
                    item.put("sku", rs.getString("SKU"));
                    item.put("quantity", rs.getInt("Quantity"));
                    item.put("price", rs.getBigDecimal("UnitPrice"));
                    item.put("lineTotal", rs.getBigDecimal("LineTotal"));

                    list.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Order> search(String keyword, String status) {
        return search(keyword, status, null, null);
    }

    public List<Order> search(
            String keyword,
            String status,
            String fromDate,
            String toDate
    ) {
        return search(keyword, status, fromDate, toDate, null, null);
    }

    public List<Order> search(
            String keyword,
            String status,
            String fromDate,
            String toDate,
            String minPrice,
            String maxPrice
    ) {
        List<Order> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT * FROM Orders WHERE 1=1 "
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("""
                    AND (
                        LOWER(RecipientName) LIKE ?
                        OR RecipientPhone LIKE ?
                        OR LOWER(OrderCode) LIKE ?
                        OR LOWER(ShippingAddress) LIKE ?
                        OR LOWER(PaymentStatus) LIKE ?
                        OR CAST(OrderID AS VARCHAR) LIKE ?
                    )
                    """);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND OrderStatus = ? ");
        }

        if (fromDate != null && !fromDate.trim().isEmpty()) {
            sql.append("AND CreatedAt >= ? ");
        }

        if (toDate != null && !toDate.trim().isEmpty()) {
            sql.append("AND CreatedAt <= ? ");
        }

        if (minPrice != null && !minPrice.trim().isEmpty()) {
            sql.append("AND TotalAmount >= ? ");
        }

        if (maxPrice != null && !maxPrice.trim().isEmpty()) {
            sql.append("AND TotalAmount <= ? ");
        }

        sql.append("ORDER BY OrderID DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String keyPattern =
                        "%" + keyword.trim().toLowerCase() + "%";

                ps.setString(paramIndex++, keyPattern);
                ps.setString(paramIndex++, keyPattern);
                ps.setString(paramIndex++, keyPattern);
                ps.setString(paramIndex++, keyPattern);
                ps.setString(paramIndex++, keyPattern);
                ps.setString(paramIndex++, keyPattern);
            }

            if (status != null && !status.trim().isEmpty()) {
                ps.setString(paramIndex++, status.trim());
            }

            if (fromDate != null && !fromDate.trim().isEmpty()) {
                ps.setString(
                        paramIndex++,
                        fromDate.trim() + " 00:00:00"
                );
            }

            if (toDate != null && !toDate.trim().isEmpty()) {
                ps.setString(
                        paramIndex++,
                        toDate.trim() + " 23:59:59"
                );
            }

            if (minPrice != null && !minPrice.trim().isEmpty()) {
                try {
                    ps.setBigDecimal(paramIndex++, new java.math.BigDecimal(minPrice.trim()));
                } catch (NumberFormatException e) {
                    ps.setNull(paramIndex++, java.sql.Types.DECIMAL);
                }
            }

            if (maxPrice != null && !maxPrice.trim().isEmpty()) {
                try {
                    ps.setBigDecimal(paramIndex++, new java.math.BigDecimal(maxPrice.trim()));
                } catch (NumberFormatException e) {
                    ps.setNull(paramIndex++, java.sql.Types.DECIMAL);
                }
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
                INSERT INTO Orders
                (
                    OrderCode,
                    CustomerID,
                    RecipientName,
                    RecipientPhone,
                    ShippingAddress,
                    SubtotalAmount,
                    DiscountAmount,
                    ShippingFee,
                    TaxAmount,
                    TotalAmount,
                    OrderStatus,
                    PaymentStatus
                )
                VALUES (?, ?, ?, ?, ?, ?, 0, 0, 0, ?, ?, ?)
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, order.getCode());

            ps.setInt(
                    2,
                    order.getUserId() > 0
                            ? order.getUserId()
                            : 4
            );

            ps.setString(
                    3,
                    order.getCustomerName() != null
                            ? order.getCustomerName()
                            : "Khách hàng WatchStore"
            );

            ps.setString(
                    4,
                    order.getPhone() != null
                            ? order.getPhone()
                            : "0988 686 868"
            );

            ps.setString(
                    5,
                    order.getShippingAddress() != null
                            ? order.getShippingAddress()
                            : "Hà Nội"
            );

            BigDecimal totalPrice =
                    order.getTotalPrice() != null
                            ? order.getTotalPrice()
                            : BigDecimal.ZERO;

            ps.setBigDecimal(6, totalPrice);
            ps.setBigDecimal(7, totalPrice);

            ps.setString(
                    8,
                    order.getStatus() != null
                            ? order.getStatus().name()
                            : "PENDING"
            );

            ps.setString(
                    9,
                    order.getPaymentStatus() != null
                            ? order.getPaymentStatus()
                            : "UNPAID"
            );

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateStatus(int id, String status) {
        String sql = """
                UPDATE Orders
                SET OrderStatus = ?
                WHERE OrderID = ?
                """;

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

    public boolean update(Order order) {
        String sql = """
                UPDATE Orders
                SET RecipientName = ?,
                    RecipientPhone = ?,
                    ShippingAddress = ?,
                    TotalAmount = ?,
                    OrderStatus = ?,
                    PaymentStatus = ?
                WHERE OrderID = ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, order.getCustomerName());
            ps.setString(2, order.getPhone());
            ps.setString(3, order.getShippingAddress());

            BigDecimal totalPrice =
                    order.getTotalPrice() != null
                            ? order.getTotalPrice()
                            : BigDecimal.ZERO;

            ps.setBigDecimal(4, totalPrice);

            ps.setString(
                    5,
                    order.getStatus() != null
                            ? order.getStatus().name()
                            : "PENDING"
            );

            ps.setString(
                    6,
                    order.getPaymentStatus() != null
                            ? order.getPaymentStatus()
                            : "UNPAID"
            );

            ps.setInt(7, order.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String deleteItemsSql =
                "DELETE FROM OrderItems WHERE OrderID = ?";

        String deleteOrderSql =
                "DELETE FROM Orders WHERE OrderID = ?";

        Connection conn = null;

        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psItems =
                         conn.prepareStatement(deleteItemsSql);
                 PreparedStatement psOrder =
                         conn.prepareStatement(deleteOrderSql)) {

                psItems.setInt(1, id);
                psItems.executeUpdate();

                psOrder.setInt(1, id);

                int affectedRows =
                        psOrder.executeUpdate();

                conn.commit();

                return affectedRows > 0;

            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }

                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;

        } finally {
            closeQuietly(conn);
        }
    }

    private Order mapResultSetToOrder(ResultSet rs)
            throws SQLException {

        Order order = new Order();

        order.setId(
                rs.getInt("OrderID")
        );

        order.setCode(
                rs.getString("OrderCode")
        );

        order.setUserId(
                rs.getInt("CustomerID")
        );

        order.setCustomerName(
                rs.getString("RecipientName")
        );

        order.setPhone(
                rs.getString("RecipientPhone")
        );

        order.setShippingAddress(
                rs.getString("ShippingAddress")
        );

        BigDecimal totalAmount =
                rs.getBigDecimal("TotalAmount");

        order.setTotalPrice(
                totalAmount != null
                        ? totalAmount
                        : BigDecimal.ZERO
        );

        order.setStatus(
                rs.getString("OrderStatus")
        );

        order.setPaymentStatus(
                rs.getString("PaymentStatus")
        );

        order.setCustomerNote(
                rs.getString("CustomerNote")
        );

        BigDecimal discountAmount =
                rs.getBigDecimal("DiscountAmount");

        order.setDiscountAmount(
                discountAmount != null
                        ? discountAmount
                        : BigDecimal.ZERO
        );

        Timestamp time =
                rs.getTimestamp("CreatedAt");

        if (time != null) {
            order.setCreatedAt(
                    new java.util.Date(
                            time.getTime()
                    )
            );
        }

        return order;
    }

    private void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ignored) {
            }
        }
    }

    public long createPOSOrder(
            Order order,
            List<Map<String, Object>> items,
            int voucherId,
            BigDecimal discountAmount,
            int staffId
    ) throws SQLException {

        String insertOrderSql = """
                INSERT INTO Orders
                (
                    OrderCode,
                    CustomerID,
                    SalesStaffID,
                    VoucherID,
                    RecipientName,
                    RecipientPhone,
                    ShippingAddress,
                    SubtotalAmount,
                    DiscountAmount,
                    ShippingFee,
                    TaxAmount,
                    TotalAmount,
                    OrderStatus,
                    PaymentStatus,
                    CreatedAt,
                    UpdatedAt
                )
                VALUES
                (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, ?,
                    'COMPLETED',
                    'PAID',
                    GETDATE(),
                    GETDATE()
                )
                """;

        String queryVariantSql = """
                SELECT
                    pv.VariantID,
                    p.ProductName,
                    pv.VariantName,
                    pv.SKU,
                    pv.SalePrice,
                    (
                        SELECT TOP 1 ImageUrl
                        FROM ProductImages
                        WHERE ProductID = p.ProductID
                        ORDER BY IsPrimary DESC, DisplayOrder ASC
                    ) AS ImageUrl,
                    ib.QuantityOnHand
                FROM ProductVariants pv
                JOIN Products p
                    ON pv.ProductID = p.ProductID
                LEFT JOIN InventoryBalances ib
                    ON pv.VariantID = ib.VariantID
                WHERE pv.VariantID = ?
                """;

        String updateStockSql = """
                UPDATE InventoryBalances
                SET QuantityOnHand = QuantityOnHand - ?
                WHERE VariantID = ?
                  AND WarehouseID = 1
                  AND QuantityOnHand >= ?
                """;

        String insertItemSql = """
                INSERT INTO OrderItems
                (
                    OrderID,
                    VariantID,
                    ProductName,
                    VariantName,
                    SKU,
                    ImageUrl,
                    UnitPrice,
                    Quantity,
                    DiscountAmount
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
                """;

        String insertTxSql = """
                INSERT INTO InventoryTransactions
                (
                    WarehouseID,
                    VariantID,
                    TransactionType,
                    QuantityChange,
                    QuantityBefore,
                    QuantityAfter,
                    ReferenceType,
                    ReferenceID,
                    Note,
                    CreatedBy,
                    CreatedAt
                )
                VALUES
                (
                    1,
                    ?,
                    'SALE',
                    ?,
                    ?,
                    ?,
                    'ORDER',
                    ?,
                    N'POS Order Checkout',
                    ?,
                    GETDATE()
                )
                """;

        String updateVoucherSql =
                "UPDATE Vouchers " +
                        "SET UsedCount = UsedCount + 1 " +
                        "WHERE VoucherID = ?";

        String insertVoucherUsageSql = """
                INSERT INTO VoucherUsages
                (
                    VoucherID,
                    UserID,
                    OrderID,
                    DiscountAmount,
                    UsedAt
                )
                VALUES (?, ?, ?, ?, GETDATE())
                """;

        Connection conn = null;

        PreparedStatement psOrder = null;
        PreparedStatement psQueryVar = null;
        PreparedStatement psUpdateStock = null;
        PreparedStatement psInsertItem = null;
        PreparedStatement psInsertTx = null;
        PreparedStatement psUpdateVoucher = null;
        PreparedStatement psInsertVoucherUsage = null;

        ResultSet rsKeys = null;

        try {
            if (order == null) {
                throw new SQLException(
                        "Thông tin đơn hàng không hợp lệ."
                );
            }

            if (items == null || items.isEmpty()) {
                throw new SQLException(
                        "Hóa đơn không có sản phẩm!"
                );
            }

            if (staffId <= 0) {
                throw new SQLException(
                        "Mã nhân viên bán hàng không hợp lệ."
                );
            }

            if (discountAmount == null) {
                discountAmount = BigDecimal.ZERO;
            }

            if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
                discountAmount = BigDecimal.ZERO;
            }

            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            BigDecimal subtotal = BigDecimal.ZERO;

            List<Map<String, Object>> validatedItems =
                    new ArrayList<>();

            psQueryVar =
                    conn.prepareStatement(queryVariantSql);

            for (Map<String, Object> item : items) {

                if (item == null) {
                    throw new SQLException(
                            "Dữ liệu sản phẩm không hợp lệ."
                    );
                }

                Object variantObject =
                        item.get("variantId");

                Object quantityObject =
                        item.get("quantity");

                if (!(variantObject instanceof Number)
                        || !(quantityObject instanceof Number)) {

                    throw new SQLException(
                            "Dữ liệu mã biến thể hoặc số lượng không hợp lệ."
                    );
                }

                int variantId =
                        ((Number) variantObject).intValue();

                int qty =
                        ((Number) quantityObject).intValue();

                if (variantId <= 0) {
                    throw new SQLException(
                            "Mã biến thể sản phẩm không hợp lệ: "
                                    + variantId
                    );
                }

                if (qty <= 0) {
                    throw new SQLException(
                            "Số lượng sản phẩm không hợp lệ: "
                                    + qty
                    );
                }

                psQueryVar.setInt(1, variantId);

                try (ResultSet rs =
                             psQueryVar.executeQuery()) {

                    if (!rs.next()) {
                        throw new SQLException(
                                "Không tìm thấy sản phẩm có mã biến thể #"
                                        + variantId
                        );
                    }

                    int stock =
                            rs.getInt("QuantityOnHand");

                    if (rs.wasNull()) {
                        stock = 0;
                    }

                    if (stock < qty) {
                        throw new SQLException(
                                "Sản phẩm '"
                                        + rs.getString("ProductName")
                                        + "' không đủ hàng trong kho (Còn "
                                        + stock
                                        + ", yêu cầu "
                                        + qty
                                        + ")"
                        );
                    }

                    BigDecimal price =
                            rs.getBigDecimal("SalePrice");

                    if (price == null) {
                        price = BigDecimal.ZERO;
                    }

                    BigDecimal lineTotal =
                            price.multiply(
                                    BigDecimal.valueOf(qty)
                            );

                    subtotal =
                            subtotal.add(lineTotal);

                    Map<String, Object> validatedItem =
                            new HashMap<>();

                    validatedItem.put(
                            "variantId",
                            variantId
                    );

                    validatedItem.put(
                            "quantity",
                            qty
                    );

                    validatedItem.put(
                            "productName",
                            rs.getString("ProductName")
                    );

                    validatedItem.put(
                            "variantName",
                            rs.getString("VariantName")
                    );

                    validatedItem.put(
                            "sku",
                            rs.getString("SKU")
                    );

                    String imageUrl =
                            rs.getString("ImageUrl");

                    if (imageUrl == null
                            || imageUrl.trim().isEmpty()) {

                        imageUrl = "default.jpg";
                    }

                    validatedItem.put(
                            "imageUrl",
                            imageUrl
                    );

                    validatedItem.put(
                            "price",
                            price
                    );

                    validatedItem.put(
                            "lineTotal",
                            lineTotal
                    );

                    validatedItem.put(
                            "stockBefore",
                            stock
                    );

                    validatedItems.add(
                            validatedItem
                    );
                }
            }

            if (validatedItems.isEmpty()) {
                throw new SQLException(
                        "Hóa đơn không có sản phẩm!"
                );
            }

            BigDecimal finalTotal =
                    subtotal.subtract(
                            discountAmount
                    );

            if (finalTotal.compareTo(
                    BigDecimal.ZERO
            ) < 0) {

                finalTotal = BigDecimal.ZERO;
            }

            psOrder =
                    conn.prepareStatement(
                            insertOrderSql,
                            Statement.RETURN_GENERATED_KEYS
                    );

            psOrder.setString(
                    1,
                    order.getCode()
            );

            psOrder.setInt(
                    2,
                    order.getUserId() > 0
                            ? order.getUserId()
                            : 4
            );

            psOrder.setInt(
                    3,
                    staffId
            );

            if (voucherId > 0) {
                psOrder.setInt(
                        4,
                        voucherId
                );
            } else {
                psOrder.setNull(
                        4,
                        Types.INTEGER
                );
            }

            psOrder.setString(
                    5,
                    order.getCustomerName() != null
                            ? order.getCustomerName()
                            : "Khách mua tại quầy"
            );

            psOrder.setString(
                    6,
                    order.getPhone() != null
                            ? order.getPhone()
                            : ""
            );

            psOrder.setString(
                    7,
                    order.getShippingAddress() != null
                            ? order.getShippingAddress()
                            : "Mua tại quầy"
            );

            psOrder.setBigDecimal(
                    8,
                    subtotal
            );

            psOrder.setBigDecimal(
                    9,
                    discountAmount
            );

            psOrder.setBigDecimal(
                    10,
                    finalTotal
            );

            psOrder.executeUpdate();

            rsKeys =
                    psOrder.getGeneratedKeys();

            long orderId = -1;

            if (rsKeys.next()) {
                orderId =
                        rsKeys.getLong(1);
            } else {
                throw new SQLException(
                        "Không lấy được mã hóa đơn tự tăng (OrderID)."
                );
            }

            psUpdateStock =
                    conn.prepareStatement(
                            updateStockSql
                    );

            psInsertItem =
                    conn.prepareStatement(
                            insertItemSql
                    );

            psInsertTx =
                    conn.prepareStatement(
                            insertTxSql
                    );

            for (Map<String, Object> validatedItem
                    : validatedItems) {

                int variantId =
                        ((Number) validatedItem.get(
                                "variantId"
                        )).intValue();

                int qty =
                        ((Number) validatedItem.get(
                                "quantity"
                        )).intValue();

                String productName =
                        (String) validatedItem.get(
                                "productName"
                        );

                String variantName =
                        (String) validatedItem.get(
                                "variantName"
                        );

                String sku =
                        (String) validatedItem.get(
                                "sku"
                        );

                String imageUrl =
                        (String) validatedItem.get(
                                "imageUrl"
                        );

                BigDecimal price =
                        (BigDecimal) validatedItem.get(
                                "price"
                        );

                int stockBefore =
                        ((Number) validatedItem.get(
                                "stockBefore"
                        )).intValue();

                psUpdateStock.setInt(
                        1,
                        qty
                );

                psUpdateStock.setInt(
                        2,
                        variantId
                );

                psUpdateStock.setInt(
                        3,
                        qty
                );

                int stockAffected =
                        psUpdateStock.executeUpdate();

                if (stockAffected == 0) {
                    throw new SQLException(
                            "Không thể cập nhật tồn kho cho VariantID #"
                                    + variantId
                                    + ". Có thể tồn kho đã thay đổi."
                    );
                }

                psInsertItem.setLong(
                        1,
                        orderId
                );

                psInsertItem.setInt(
                        2,
                        variantId
                );

                psInsertItem.setString(
                        3,
                        productName
                );

                psInsertItem.setString(
                        4,
                        variantName
                );

                psInsertItem.setString(
                        5,
                        sku
                );

                psInsertItem.setString(
                        6,
                        imageUrl
                );

                psInsertItem.setBigDecimal(
                        7,
                        price
                );

                psInsertItem.setInt(
                        8,
                        qty
                );

                psInsertItem.executeUpdate();

                psInsertTx.setInt(
                        1,
                        variantId
                );

                psInsertTx.setInt(
                        2,
                        qty
                );

                psInsertTx.setInt(
                        3,
                        stockBefore
                );

                psInsertTx.setInt(
                        4,
                        stockBefore - qty
                );

                psInsertTx.setLong(
                        5,
                        orderId
                );

                psInsertTx.setInt(
                        6,
                        staffId
                );

                psInsertTx.executeUpdate();
            }

            if (voucherId > 0) {

                psUpdateVoucher =
                        conn.prepareStatement(
                                updateVoucherSql
                        );

                psUpdateVoucher.setInt(
                        1,
                        voucherId
                );

                psUpdateVoucher.executeUpdate();

                psInsertVoucherUsage =
                        conn.prepareStatement(
                                insertVoucherUsageSql
                        );

                psInsertVoucherUsage.setInt(
                        1,
                        voucherId
                );

                psInsertVoucherUsage.setInt(
                        2,
                        order.getUserId() > 0
                                ? order.getUserId()
                                : 4
                );

                psInsertVoucherUsage.setLong(
                        3,
                        orderId
                );

                psInsertVoucherUsage.setBigDecimal(
                        4,
                        discountAmount
                );

                psInsertVoucherUsage.executeUpdate();
            }

            conn.commit();

            return orderId;

        } catch (SQLException e) {

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            e.printStackTrace();

            throw e;

        } finally {

            closeQuietly(rsKeys);
            closeQuietly(psOrder);
            closeQuietly(psQueryVar);
            closeQuietly(psUpdateStock);
            closeQuietly(psInsertItem);
            closeQuietly(psInsertTx);
            closeQuietly(psUpdateVoucher);
            closeQuietly(psInsertVoucherUsage);
            closeQuietly(conn);
        }
    }

    public boolean cancelOrderAndRestoreStock(int orderId) {

        String checkOrderSql = """
                SELECT OrderStatus
                FROM Orders
                WHERE OrderID = ?
                """;

        String updateOrderSql = """
                UPDATE Orders
                SET OrderStatus = 'CANCELLED',
                    CancelledAt = GETDATE()
                WHERE OrderID = ?
                """;

        String queryItemsSql = """
                SELECT VariantID, Quantity
                FROM OrderItems
                WHERE OrderID = ?
                """;

        String queryStockSql = """
                SELECT QuantityOnHand
                FROM InventoryBalances
                WHERE VariantID = ?
                  AND WarehouseID = 1
                """;

        String updateStockSql = """
                UPDATE InventoryBalances
                SET QuantityOnHand = QuantityOnHand + ?
                WHERE VariantID = ?
                  AND WarehouseID = 1
                """;

        String insertTxSql = """
                INSERT INTO InventoryTransactions
                (
                    WarehouseID,
                    VariantID,
                    TransactionType,
                    QuantityChange,
                    QuantityBefore,
                    QuantityAfter,
                    ReferenceType,
                    ReferenceID,
                    Note,
                    CreatedAt
                )
                VALUES
                (
                    1,
                    ?,
                    'RETURN_IN',
                    ?,
                    ?,
                    ?,
                    'CANCEL',
                    ?,
                    N'Order Cancelled, Stock Restored',
                    GETDATE()
                )
                """;

        Connection conn = null;

        PreparedStatement psCheckOrder = null;
        PreparedStatement psUpdateOrder = null;
        PreparedStatement psQueryItems = null;
        PreparedStatement psQueryStock = null;
        PreparedStatement psUpdateStock = null;
        PreparedStatement psInsertTx = null;

        ResultSet rsItems = null;

        try {
            if (orderId <= 0) {
                return false;
            }

            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            psCheckOrder =
                    conn.prepareStatement(
                            checkOrderSql
                    );

            psCheckOrder.setInt(
                    1,
                    orderId
            );

            try (ResultSet rs =
                         psCheckOrder.executeQuery()) {

                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }

                String currentStatus =
                        rs.getString("OrderStatus");

                if ("CANCELLED".equalsIgnoreCase(
                        currentStatus
                )) {
                    conn.rollback();
                    return false;
                }
            }

            psUpdateOrder =
                    conn.prepareStatement(
                            updateOrderSql
                    );

            psUpdateOrder.setInt(
                    1,
                    orderId
            );

            int affected =
                    psUpdateOrder.executeUpdate();

            if (affected == 0) {
                conn.rollback();
                return false;
            }

            psQueryItems =
                    conn.prepareStatement(
                            queryItemsSql
                    );

            psQueryItems.setInt(
                    1,
                    orderId
            );

            rsItems =
                    psQueryItems.executeQuery();

            psQueryStock =
                    conn.prepareStatement(
                            queryStockSql
                    );

            psUpdateStock =
                    conn.prepareStatement(
                            updateStockSql
                    );

            psInsertTx =
                    conn.prepareStatement(
                            insertTxSql
                    );

            while (rsItems.next()) {

                int variantId =
                        rsItems.getInt(
                                "VariantID"
                        );

                int qty =
                        rsItems.getInt(
                                "Quantity"
                        );

                if (variantId <= 0 || qty <= 0) {
                    continue;
                }

                psQueryStock.setInt(
                        1,
                        variantId
                );

                int stockBefore;

                try (ResultSet rsStock =
                             psQueryStock.executeQuery()) {

                    if (!rsStock.next()) {
                        throw new SQLException(
                                "Không tìm thấy tồn kho cho VariantID #"
                                        + variantId
                        );
                    }

                    stockBefore =
                            rsStock.getInt(
                                    "QuantityOnHand"
                            );
                }

                psUpdateStock.setInt(
                        1,
                        qty
                );

                psUpdateStock.setInt(
                        2,
                        variantId
                );

                int stockAffected =
                        psUpdateStock.executeUpdate();

                if (stockAffected == 0) {
                    throw new SQLException(
                            "Không thể phục hồi tồn kho cho VariantID #"
                                    + variantId
                    );
                }

                psInsertTx.setInt(
                        1,
                        variantId
                );

                psInsertTx.setInt(
                        2,
                        qty
                );

                psInsertTx.setInt(
                        3,
                        stockBefore
                );

                psInsertTx.setInt(
                        4,
                        stockBefore + qty
                );

                psInsertTx.setInt(
                        5,
                        orderId
                );

                psInsertTx.executeUpdate();
            }

            conn.commit();

            return true;

        } catch (SQLException e) {

            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            return false;

        } finally {

            closeQuietly(rsItems);
            closeQuietly(psCheckOrder);
            closeQuietly(psUpdateOrder);
            closeQuietly(psQueryItems);
            closeQuietly(psQueryStock);
            closeQuietly(psUpdateStock);
            closeQuietly(psInsertTx);
            closeQuietly(conn);
        }
    }

    public long createSalesOrder(
            Order order,
            List<Map<String, Object>> items,
            int staffId
    ) throws SQLException {
        String insertOrderSql = """
                INSERT INTO Orders
                (
                    OrderCode, CustomerID, RecipientName, RecipientPhone, ShippingAddress,
                    CustomerNote, SubtotalAmount, DiscountAmount, ShippingFee, TaxAmount, TotalAmount,
                    OrderStatus, PaymentStatus, CreatedAt, UpdatedAt
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, 0, ?, ?, ?, GETDATE(), GETDATE())
                """;

        String queryVariantSql = """
                SELECT pv.VariantID, p.ProductName, pv.VariantName, pv.SKU, pv.SalePrice,
                       ib.QuantityOnHand
                FROM ProductVariants pv
                JOIN Products p ON pv.ProductID = p.ProductID
                LEFT JOIN InventoryBalances ib ON pv.VariantID = ib.VariantID
                WHERE pv.VariantID = ?
                """;

        String updateStockSql = """
                UPDATE InventoryBalances
                SET QuantityOnHand = QuantityOnHand - ?
                WHERE VariantID = ? AND WarehouseID = 1 AND QuantityOnHand >= ?
                """;

        String insertItemSql = """
                INSERT INTO OrderItems
                (
                    OrderID, VariantID, ProductName, VariantName, SKU, ImageUrl, UnitPrice, Quantity, DiscountAmount
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
                """;

        String insertTxSql = """
                INSERT INTO InventoryTransactions
                (
                    WarehouseID, VariantID, TransactionType, QuantityChange,
                    QuantityBefore, QuantityAfter, ReferenceType, ReferenceID, Note, CreatedBy, CreatedAt
                )
                VALUES (1, ?, 'SALE', ?, ?, ?, 'ORDER', ?, N'Sales Order Checkout', ?, GETDATE())
                """;

        Connection conn = null;
        PreparedStatement psOrder = null;
        PreparedStatement psQueryVar = null;
        PreparedStatement psUpdateStock = null;
        PreparedStatement psInsertItem = null;
        PreparedStatement psInsertTx = null;
        ResultSet rsKeys = null;

        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            psOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            psOrder.setString(1, order.getCode());
            psOrder.setInt(2, order.getUserId() > 0 ? order.getUserId() : 4);
            psOrder.setString(3, order.getCustomerName());
            psOrder.setString(4, order.getPhone());
            psOrder.setString(5, order.getShippingAddress());
            psOrder.setString(6, order.getCustomerNote());
            psOrder.setBigDecimal(7, order.getTotalPrice());
            psOrder.setBigDecimal(8, order.getTotalPrice());
            psOrder.setString(9, order.getStatus() != null ? order.getStatus().name() : "PENDING");
            psOrder.setString(10, order.getPaymentStatus() != null ? order.getPaymentStatus() : "UNPAID");

            psOrder.executeUpdate();
            rsKeys = psOrder.getGeneratedKeys();

            long orderId = 0;
            if (rsKeys.next()) {
                orderId = rsKeys.getLong(1);
            } else {
                throw new SQLException("Lỗi hệ thống: Không lấy được OrderID vừa tạo.");
            }

            psQueryVar = conn.prepareStatement(queryVariantSql);
            psUpdateStock = conn.prepareStatement(updateStockSql);
            psInsertItem = conn.prepareStatement(insertItemSql);
            psInsertTx = conn.prepareStatement(insertTxSql);

            for (Map<String, Object> item : items) {
                int variantId = ((Number) item.get("variantId")).intValue();
                int qty = ((Number) item.get("quantity")).intValue();

                psQueryVar.setInt(1, variantId);
                try (ResultSet rs = psQueryVar.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Không tìm thấy sản phẩm có mã biến thể #" + variantId);
                    }

                    int stock = rs.getInt("QuantityOnHand");
                    if (rs.wasNull()) stock = 0;

                    if (stock < qty) {
                        throw new SQLException("Sản phẩm '" + rs.getString("ProductName") + "' không đủ hàng trong kho (Còn " + stock + ", yêu cầu " + qty + ")");
                    }

                    BigDecimal price = null;
                    if (item.containsKey("unitPrice") && ((Number) item.get("unitPrice")).doubleValue() >= 0) {
                        price = BigDecimal.valueOf(((Number) item.get("unitPrice")).doubleValue());
                    } else {
                        price = rs.getBigDecimal("SalePrice");
                    }
                    if (price == null) price = BigDecimal.ZERO;

                    // Update stock
                    psUpdateStock.setInt(1, qty);
                    psUpdateStock.setInt(2, variantId);
                    psUpdateStock.setInt(3, qty);
                    int affected = psUpdateStock.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("Không thể trừ tồn kho cho sản phẩm mã biến thể #" + variantId);
                    }

                    // Insert OrderItem
                    psInsertItem.setLong(1, orderId);
                    psInsertItem.setInt(2, variantId);
                    psInsertItem.setString(3, rs.getString("ProductName"));
                    psInsertItem.setString(4, rs.getString("VariantName"));
                    psInsertItem.setString(5, rs.getString("SKU"));
                    psInsertItem.setString(6, "seiko-5.jpg");
                    psInsertItem.setBigDecimal(7, price);
                    psInsertItem.setInt(8, qty);
                    psInsertItem.executeUpdate();

                    // Insert InventoryTransaction
                    psInsertTx.setInt(1, variantId);
                    psInsertTx.setInt(2, -qty);
                    psInsertTx.setInt(3, stock);
                    psInsertTx.setInt(4, stock - qty);
                    psInsertTx.setLong(5, orderId);
                    psInsertTx.setInt(6, staffId);
                    psInsertTx.executeUpdate();
                }
            }

            conn.commit();
            return orderId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            closeQuietly(rsKeys);
            closeQuietly(psOrder);
            closeQuietly(psQueryVar);
            closeQuietly(psUpdateStock);
            closeQuietly(psInsertItem);
            closeQuietly(psInsertTx);
            closeQuietly(conn);
        }
    }
}