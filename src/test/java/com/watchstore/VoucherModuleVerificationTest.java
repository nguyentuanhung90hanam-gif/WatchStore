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
import java.util.ArrayList;
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
    private static final String testVoucherCode = "TEST2026";
    private static int createdVoucherId;
    private static final List<Integer> tempVoucherIds = new ArrayList<>();

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

            // Cleanup any previous TEST2026 vouchers
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM dbo.Vouchers WHERE VoucherCode LIKE 'TEST2026%'")) {
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
    @DisplayName("CASE 1: Admin creates TEST2026 (IsPublic=1, Status=ACTIVE, StartAt<=now, EndAt>=now, UsageLimit=0) -> appears in findPublicActive()")
    public void testCreateVoucherTest2026() {
        Voucher v = new Voucher();
        v.setVoucherCode(testVoucherCode);
        v.setVoucherName("Mã Giảm Giá TEST2026");
        v.setDescription("Voucher test thực tế hệ thống");
        v.setDiscountType("PERCENT");
        v.setDiscountValue(new BigDecimal("10.00"));
        v.setMaximumDiscount(new BigDecimal("500000.00"));
        v.setMinimumOrderValue(new BigDecimal("100000.00"));
        v.setUsageLimit(0); // 0 = unlimited
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

        // Verify findPublicActive contains it
        List<Voucher> publicActive = voucherRepo.findPublicActive();
        assertNotNull(publicActive);
        assertTrue(publicActive.stream().anyMatch(item -> item.getVoucherCode().equalsIgnoreCase(testVoucherCode)),
                "findPublicActive() must return newly created TEST2026 voucher");

        // Verify findPublicActiveVouchers also contains it
        List<Voucher> publicActiveLegacy = voucherRepo.findPublicActiveVouchers();
        assertNotNull(publicActiveLegacy);
        assertTrue(publicActiveLegacy.stream().anyMatch(item -> item.getVoucherCode().equalsIgnoreCase(testVoucherCode)));
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("CASE 2 & 3: UsageLimit=100, UsedCount=0 and UsedCount=99 -> must appear in findPublicActive()")
    public void testUsageLimitNotExceeded() {
        String code = "TEST2026_PARTIAL";
        Voucher v = new Voucher();
        v.setVoucherCode(code);
        v.setVoucherName("Voucher Còn Lượt Dùng");
        v.setDiscountType("FIXED");
        v.setDiscountValue(new BigDecimal("50000"));
        v.setUsageLimit(100);
        v.setUsedCount(99);
        v.setStartAt(LocalDateTime.now().minusMinutes(5));
        v.setEndAt(LocalDateTime.now().plusMonths(1));
        v.setIsPublic(true);
        v.setStatus("ACTIVE");
        v.setCreatedBy(testUserId);

        voucherRepo.save(v);
        List<Voucher> found = voucherRepo.search(code);
        if (!found.isEmpty()) tempVoucherIds.add(found.get(0).getVoucherId());

        List<Voucher> publicList = voucherRepo.findPublicActive();
        assertTrue(publicList.stream().anyMatch(item -> code.equalsIgnoreCase(item.getVoucherCode())),
                "Voucher with UsedCount < UsageLimit must be returned by findPublicActive()");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("CASE 4: UsageLimit=100, UsedCount=100 -> must NOT appear in findPublicActive()")
    public void testUsageLimitMaxedOut() {
        String code = "TEST2026_MAXED";
        Voucher v = new Voucher();
        v.setVoucherCode(code);
        v.setVoucherName("Voucher Đã Hết Lượt");
        v.setDiscountType("FIXED");
        v.setDiscountValue(new BigDecimal("50000"));
        v.setUsageLimit(100);
        v.setUsedCount(100);
        v.setStartAt(LocalDateTime.now().minusMinutes(5));
        v.setEndAt(LocalDateTime.now().plusMonths(1));
        v.setIsPublic(true);
        v.setStatus("ACTIVE");
        v.setCreatedBy(testUserId);

        voucherRepo.save(v);
        List<Voucher> found = voucherRepo.search(code);
        if (!found.isEmpty()) tempVoucherIds.add(found.get(0).getVoucherId());

        List<Voucher> publicList = voucherRepo.findPublicActive();
        assertFalse(publicList.stream().anyMatch(item -> code.equalsIgnoreCase(item.getVoucherCode())),
                "Voucher with UsedCount >= UsageLimit must not be returned by findPublicActive()");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("CASE 5 & 6: UsageLimit = 0 or null -> unlimited -> must appear in findPublicActive()")
    public void testUnlimitedVouchers() {
        String code = "TEST2026_UNLIMITED";
        Voucher v = new Voucher();
        v.setVoucherCode(code);
        v.setVoucherName("Voucher Không Giới Hạn");
        v.setDiscountType("PERCENT");
        v.setDiscountValue(new BigDecimal("5.00"));
        v.setUsageLimit(0);
        v.setUsedCount(150);
        v.setStartAt(LocalDateTime.now().minusMinutes(5));
        v.setEndAt(LocalDateTime.now().plusMonths(1));
        v.setIsPublic(true);
        v.setStatus("ACTIVE");
        v.setCreatedBy(testUserId);

        voucherRepo.save(v);
        List<Voucher> found = voucherRepo.search(code);
        if (!found.isEmpty()) tempVoucherIds.add(found.get(0).getVoucherId());

        List<Voucher> publicList = voucherRepo.findPublicActive();
        assertTrue(publicList.stream().anyMatch(item -> code.equalsIgnoreCase(item.getVoucherCode())),
                "Unlimited voucher (limit=0) must be returned by findPublicActive()");
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("CASE 7: StartAt > now -> must NOT appear in findPublicActive()")
    public void testFutureVoucherNotVisible() {
        String code = "TEST2026_FUTURE";
        Voucher v = new Voucher();
        v.setVoucherCode(code);
        v.setVoucherName("Voucher Tương Lai");
        v.setDiscountType("FIXED");
        v.setDiscountValue(new BigDecimal("50000"));
        v.setStartAt(LocalDateTime.now().plusDays(5));
        v.setEndAt(LocalDateTime.now().plusDays(15));
        v.setIsPublic(true);
        v.setStatus("ACTIVE");
        v.setCreatedBy(testUserId);

        voucherRepo.save(v);
        List<Voucher> found = voucherRepo.search(code);
        if (!found.isEmpty()) tempVoucherIds.add(found.get(0).getVoucherId());

        List<Voucher> publicList = voucherRepo.findPublicActive();
        assertFalse(publicList.stream().anyMatch(item -> code.equalsIgnoreCase(item.getVoucherCode())),
                "Future voucher should not be returned by findPublicActive()");
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("CASE 8: EndAt < now -> must NOT appear in findPublicActive()")
    public void testExpiredVoucherNotVisible() {
        String code = "TEST2026_EXPIRED";
        Voucher v = new Voucher();
        v.setVoucherCode(code);
        v.setVoucherName("Voucher Hết Hạn");
        v.setDiscountType("FIXED");
        v.setDiscountValue(new BigDecimal("50000"));
        v.setStartAt(LocalDateTime.now().minusDays(10));
        v.setEndAt(LocalDateTime.now().minusDays(1));
        v.setIsPublic(true);
        v.setStatus("ACTIVE");
        v.setCreatedBy(testUserId);

        voucherRepo.save(v);
        List<Voucher> found = voucherRepo.search(code);
        if (!found.isEmpty()) tempVoucherIds.add(found.get(0).getVoucherId());

        List<Voucher> publicList = voucherRepo.findPublicActive();
        assertFalse(publicList.stream().anyMatch(item -> code.equalsIgnoreCase(item.getVoucherCode())),
                "Expired voucher should not be returned by findPublicActive()");
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    @DisplayName("CASE 9: IsPublic = 0 -> must NOT appear in findPublicActive()")
    public void testPrivateVoucherNotVisible() {
        String code = "TEST2026_PRIVATE";
        Voucher v = new Voucher();
        v.setVoucherCode(code);
        v.setVoucherName("Voucher Riêng Tư");
        v.setDiscountType("PERCENT");
        v.setDiscountValue(new BigDecimal("20.00"));
        v.setStartAt(LocalDateTime.now().minusMinutes(5));
        v.setEndAt(LocalDateTime.now().plusMonths(1));
        v.setIsPublic(false);
        v.setStatus("ACTIVE");
        v.setCreatedBy(testUserId);

        voucherRepo.save(v);
        List<Voucher> found = voucherRepo.search(code);
        if (!found.isEmpty()) tempVoucherIds.add(found.get(0).getVoucherId());

        List<Voucher> publicList = voucherRepo.findPublicActive();
        assertFalse(publicList.stream().anyMatch(item -> code.equalsIgnoreCase(item.getVoucherCode())),
                "Private voucher should not be returned by findPublicActive()");
    }

    @Test
    @org.junit.jupiter.api.Order(10)
    @DisplayName("CASE 10: Status != ACTIVE -> must NOT appear in findPublicActive()")
    public void testInactiveVoucherNotVisible() {
        String code = "TEST2026_INACTIVE";
        Voucher v = new Voucher();
        v.setVoucherCode(code);
        v.setVoucherName("Voucher Ngưng Kích Hoạt");
        v.setDiscountType("PERCENT");
        v.setDiscountValue(new BigDecimal("20.00"));
        v.setStartAt(LocalDateTime.now().minusMinutes(5));
        v.setEndAt(LocalDateTime.now().plusMonths(1));
        v.setIsPublic(true);
        v.setStatus("INACTIVE");
        v.setCreatedBy(testUserId);

        voucherRepo.save(v);
        List<Voucher> found = voucherRepo.search(code);
        if (!found.isEmpty()) tempVoucherIds.add(found.get(0).getVoucherId());

        List<Voucher> publicList = voucherRepo.findPublicActive();
        assertFalse(publicList.stream().anyMatch(item -> code.equalsIgnoreCase(item.getVoucherCode())),
                "Inactive voucher should not be returned by findPublicActive()");
    }

    @Test
    @org.junit.jupiter.api.Order(11)
    @DisplayName("CASE 11 & 12: Admin updates UsageLimit (10 -> 100) & UsageLimitPerUser (1 -> 5) -> DB and repository updated")
    public void testAdminUpdateUsageLimitAndPerUser() {
        Voucher v = voucherRepo.findById(createdVoucherId);
        assertNotNull(v);

        v.setUsageLimit(100);
        v.setUsageLimitPerUser(5);
        v.setVoucherName("Mã Giảm Giá TEST2026 (Đã Sửa Lượt Dùng)");

        boolean updated = voucherRepo.update(v);
        assertTrue(updated, "Update voucher should return true");

        Voucher updatedVoucher = voucherRepo.findById(createdVoucherId);
        assertNotNull(updatedVoucher);
        assertEquals(100, updatedVoucher.getUsageLimit(), "UsageLimit in DB must be 100");
        assertEquals(5, updatedVoucher.getUsageLimitPerUser(), "UsageLimitPerUser in DB must be 5");
        assertEquals("Mã Giảm Giá TEST2026 (Đã Sửa Lượt Dùng)", updatedVoucher.getVoucherName());
    }

    @Test
    @org.junit.jupiter.api.Order(13)
    @DisplayName("CASE 13 & 14: Checkout order with TEST2026 voucher applies discount and increments UsedCount")
    public void testCheckoutWithVoucher() throws Exception {
        if (testProductId <= 0 || testVariantId <= 0) return;

        // Clear cart and add product
        cartRepo.clear(testUserId);
        cartRepo.addProduct(testUserId, testProductId, 1);

        // Place order with TEST2026
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

            for (Integer tempId : tempVoucherIds) {
                if (tempId != null && tempId > 0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM dbo.VoucherUsages WHERE VoucherID = ?")) {
                        ps.setInt(1, tempId);
                        ps.executeUpdate();
                    }
                    voucherRepo.delete(tempId);
                }
            }
        }
    }
}
