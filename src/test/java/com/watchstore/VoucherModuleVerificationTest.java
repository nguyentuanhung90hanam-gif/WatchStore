package com.watchstore;

import com.watchstore.config.DBContext;
import com.watchstore.model.Order;
import com.watchstore.model.Voucher;
import com.watchstore.repository.CartRepository;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.VoucherRepository;
import com.watchstore.repository.VoucherRepositoryImpl;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VoucherModuleVerificationTest {

    private static VoucherRepository voucherRepo;
    private static OrderRepository orderRepo;
    private static CartRepository cartRepo;

    private static int testUserId;
    private static int testAddressId;
    private static int testProductId;
    private static int testVariantId;
    private static String testVoucherCode = "TESTDEMO10";
    private static int createdVoucherId;

    @BeforeAll
    public static void setUp() throws Exception {
        voucherRepo = new VoucherRepositoryImpl();
        orderRepo = new OrderRepository();
        cartRepo = new CartRepository();

        try (Connection conn = DBContext.getConnection()) {
            // Get or create test user
            testUserId = getOrCreateTestUser(conn, "voucher_test_user@example.com", "Voucher Tester");

            // Get or create test address
            testAddressId = getOrCreateTestAddress(conn, testUserId);

            // Get valid product & variant
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT TOP 1 pv.ProductID, pv.VariantID FROM dbo.ProductVariants pv JOIN dbo.Products p ON p.ProductID=pv.ProductID WHERE pv.Status='ACTIVE' AND p.Status='ACTIVE' ORDER BY pv.ProductID ASC");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    testProductId = rs.getInt("ProductID");
                    testVariantId = rs.getInt("VariantID");
                }
            }

            // Cleanup any previous TESTDEMO10 vouchers
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM dbo.Vouchers WHERE VoucherCode = ?")) {
                ps.setString(1, testVoucherCode);
                ps.executeUpdate();
            }
        }
    }

    private static int getOrCreateTestUser(Connection conn, String email, String fullName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT UserID FROM dbo.Users WHERE Email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("UserID");
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO dbo.Users (Email, PasswordHash, FullName, Status, CreatedAt, UpdatedAt) VALUES (?, 'test_hash', ?, 'ACTIVE', SYSDATETIME(), SYSDATETIME())",
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email);
            ps.setString(2, fullName);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 1;
    }

    private static int getOrCreateTestAddress(Connection conn, int userId) throws SQLException {
        com.watchstore.repository.AddressRepository addressRepo = new com.watchstore.repository.AddressRepository();
        List<Map<String, Object>> addrs = addressRepo.findAll(userId);
        if (!addrs.isEmpty()) {
            return (int) addrs.get(0).get("id");
        }
        addressRepo.save(userId, null, "Người nhận test", "0988776655", "TP. Hồ Chí Minh", "Quận 1", "Phường 1", "123 Đường Test", "HOME", true);
        addrs = addressRepo.findAll(userId);
        return (int) addrs.get(0).get("id");
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("1. Admin creates TESTDEMO10 voucher in SQL Server")
    public void testCreateVoucher() {
        Voucher v = new Voucher();
        v.setVoucherCode(testVoucherCode);
        v.setVoucherName("Giảm 10% Cho Khách Thử Nghiệm");
        v.setDescription("Voucher test đặc biệt");
        v.setDiscountType("PERCENT");
        v.setDiscountValue(new BigDecimal("10.00"));
        v.setMaximumDiscount(new BigDecimal("500000.00"));
        v.setMinimumOrderValue(new BigDecimal("100000.00"));
        v.setUsageLimit(50);
        v.setUsageLimitPerUser(2);
        v.setUsedCount(0);
        v.setStartAt(LocalDateTime.now().minusMinutes(5));
        v.setEndAt(LocalDateTime.now().plusMonths(2));
        v.setIsPublic(true);
        v.setStatus("ACTIVE");
        v.setCreatedBy(testUserId);

        boolean saved = voucherRepo.save(v);
        assertTrue(saved, "Voucher save should return true");

        // Verify it exists in DB
        List<Voucher> found = voucherRepo.search(testVoucherCode);
        assertNotNull(found);
        assertFalse(found.isEmpty(), "Found vouchers should not be empty");
        Voucher created = found.get(0);
        assertEquals(testVoucherCode, created.getVoucherCode());
        assertEquals("ACTIVE", created.getStatus());
        assertTrue(created.getIsPublic());
        createdVoucherId = created.getVoucherId();
        assertTrue(createdVoucherId > 0);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("2. Find Voucher By ID and Search")
    public void testFindByIdAndSearch() {
        Voucher v = voucherRepo.findById(createdVoucherId);
        assertNotNull(v, "Voucher by ID should not be null");
        assertEquals(testVoucherCode, v.getVoucherCode());
        assertEquals(new BigDecimal("10.00"), v.getDiscountValue());

        List<Voucher> searchResults = voucherRepo.search("TESTDEMO");
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.stream().anyMatch(item -> item.getVoucherCode().equals(testVoucherCode)));
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("3. Verify findPublicActiveVouchers contains TESTDEMO10")
    public void testPublicActiveVouchers() {
        List<Voucher> publicList = voucherRepo.findPublicActiveVouchers();
        assertNotNull(publicList, "Public active vouchers should not be null");
        boolean containsTestVoucher = publicList.stream()
                .anyMatch(v -> testVoucherCode.equalsIgnoreCase(v.getVoucherCode()));
        assertTrue(containsTestVoucher, "Public active vouchers must contain TESTDEMO10");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("4. Update TESTDEMO10 voucher (e.g. increase discount)")
    public void testUpdateVoucher() {
        Voucher v = voucherRepo.findById(createdVoucherId);
        assertNotNull(v);
        v.setVoucherName("Giảm 15% Cho Khách Thử Nghiệm (Đã Cập Nhật)");
        v.setDiscountValue(new BigDecimal("15.00"));
        boolean updated = voucherRepo.update(v);
        assertTrue(updated, "Update voucher should return true");

        Voucher updatedVoucher = voucherRepo.findById(createdVoucherId);
        assertEquals("Giảm 15% Cho Khách Thử Nghiệm (Đã Cập Nhật)", updatedVoucher.getVoucherName());
        assertEquals(new BigDecimal("15.00"), updatedVoucher.getDiscountValue());
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("5. Checkout order with TESTDEMO10 voucher calculates discount and records usage")
    public void testCheckoutWithVoucher() throws Exception {
        if (testProductId <= 0 || testVariantId <= 0) return;

        // Clear cart and add product
        cartRepo.clear(testUserId);
        cartRepo.addProduct(testUserId, testProductId, 1);

        // Place order with TESTDEMO10
        long orderId = orderRepo.createFromCart(testUserId, testAddressId, testVoucherCode, "COD", "Giao nhanh");
        assertTrue(orderId > 0, "Created OrderID should be greater than 0");

        Order order = orderRepo.findById((int) orderId);
        assertNotNull(order, "Order should not be null");
        assertTrue(order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0, "Discount should be applied (> 0)");

        // Verify usedCount incremented in Vouchers table
        Voucher v = voucherRepo.findById(createdVoucherId);
        assertEquals(1, v.getUsedCount(), "Used count should be incremented to 1");

        // Verify VoucherUsage record
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM dbo.VoucherUsages WHERE OrderID = ? AND VoucherID = ?")) {
            ps.setLong(1, orderId);
            ps.setInt(2, createdVoucherId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "VoucherUsage record must exist");
                assertEquals(testUserId, rs.getInt("UserID"));
                assertTrue(rs.getBigDecimal("DiscountAmount").compareTo(BigDecimal.ZERO) > 0);
            }
        }
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("6. Checkout with non-existent or expired voucher must throw exception and not create order")
    public void testCheckoutWithInvalidVoucher() throws Exception {
        if (testProductId <= 0 || testVariantId <= 0) return;

        cartRepo.clear(testUserId);
        cartRepo.addProduct(testUserId, testProductId, 1);

        assertThrows(Exception.class, () -> {
            orderRepo.createFromCart(testUserId, testAddressId, "INVALID_VOUCHER_CODE_9999", "COD", "Note");
        }, "Should throw exception on invalid voucher");
    }

    @AfterAll
    public static void tearDown() throws Exception {
        // Clean up test voucher usages & orders
        try (Connection conn = DBContext.getConnection()) {
            if (createdVoucherId > 0) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM dbo.VoucherUsages WHERE VoucherID = ?")) {
                    ps.setInt(1, createdVoucherId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("UPDATE dbo.Orders SET VoucherID = NULL WHERE VoucherID = ?")) {
                    ps.setInt(1, createdVoucherId);
                    ps.executeUpdate();
                }
                voucherRepo.delete(createdVoucherId);
            }
        }
    }
}
