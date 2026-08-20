package com.watchstore;

import com.watchstore.config.DBContext;
import com.watchstore.enums.OrderStatus;
import com.watchstore.model.Order;
import com.watchstore.model.Product;
import com.watchstore.model.ProductComment;
import com.watchstore.model.Review;
import com.watchstore.repository.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerModuleVerificationTest {

    private static CartRepository cartRepo;
    private static OrderRepository orderRepo;
    private static AddressRepository addressRepo;
    private static WishlistRepository wishlistRepo;
    private static ReviewRepository reviewRepo;
    private static CommentRepository commentRepo;
    private static ProductRepository productRepo;

    private static int customerA_id;
    private static int customerB_id;
    private static int sampleProductId;
    private static int sampleVariantId;
    private static int sampleAddressId;
    private static long createdOrderId;

    @BeforeAll
    public static void setUp() throws Exception {
        cartRepo = new CartRepository();
        orderRepo = new OrderRepository();
        addressRepo = new AddressRepository();
        wishlistRepo = new WishlistRepository();
        reviewRepo = new ReviewRepositoryImpl();
        commentRepo = new CommentRepositoryImpl();
        productRepo = new ProductRepositoryImpl();

        // 1. Get or create test customer A & B
        try (Connection conn = DBContext.getConnection()) {
            customerA_id = getOrCreateTestUser(conn, "test_customer_a@example.com", "Customer A");
            customerB_id = getOrCreateTestUser(conn, "test_customer_b@example.com", "Customer B");

            // Get a valid product & variant
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT TOP 1 pv.ProductID, pv.VariantID FROM dbo.ProductVariants pv JOIN dbo.Products p ON p.ProductID=pv.ProductID WHERE pv.Status='ACTIVE' AND p.Status='ACTIVE' ORDER BY pv.ProductID ASC");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sampleProductId = rs.getInt("ProductID");
                    sampleVariantId = rs.getInt("VariantID");
                }
            }

            // Create a test address for customer A
            addressRepo.save(customerA_id, null, "Customer A Recipient", "0912345678", "Hà Nội", "Ba Đình", "Kim Mã", "123 Kim Mã", "HOME", true);
            List<Map<String, Object>> addrs = addressRepo.findAll(customerA_id);
            assertFalse(addrs.isEmpty(), "Customer A should have at least 1 address");
            sampleAddressId = (int) addrs.get(0).get("id");
        }
    }

    private static int getOrCreateTestUser(Connection conn, String email, String fullName) throws SQLException {
        String sel = "SELECT UserID FROM dbo.Users WHERE Email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sel)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("UserID");
            }
        }
        String ins = "INSERT INTO dbo.Users (Email, PasswordHash, FullName, Status, CreatedAt, UpdatedAt) VALUES (?, 'hash123', ?, 'ACTIVE', SYSDATETIME(), SYSDATETIME())";
        try (PreparedStatement ps = conn.prepareStatement(ins, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email);
            ps.setString(2, fullName);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to create test user " + email);
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("TEST 1: Guest xem products")
    public void testGuestViewProducts() {
        List<Product> products = productRepo.findAll();
        assertNotNull(products);
        assertFalse(products.isEmpty(), "Product list should not be empty");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("TEST 2: Guest filter products")
    public void testGuestFilterProducts() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setSort("price-asc");
        ProductPage page = productRepo.search(criteria);
        assertNotNull(page);
        assertNotNull(page.getItems());
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("TEST 3: Guest xem product detail")
    public void testGuestViewProductDetail() {
        var opt = productRepo.findById(sampleProductId);
        assertTrue(opt.isPresent());
        assertEquals(sampleProductId, opt.get().getProductId());
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("TEST 4: Guest xem public review")
    public void testGuestViewPublicReview() {
        List<Review> reviews = reviewRepo.findByProductId(sampleProductId);
        assertNotNull(reviews);
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("TEST 5: Guest xem public comment")
    public void testGuestViewPublicComment() {
        List<ProductComment> comments = commentRepo.findByProductId(sampleProductId);
        assertNotNull(comments);
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("TEST 6-8: Guest actions require authentication")
    public void testGuestActionsBlocked() {
        // Cart and Order repositories enforce userId > 0
        assertThrows(IllegalArgumentException.class, () -> cartRepo.add(0, sampleVariantId, 1));
        assertThrows(IllegalArgumentException.class, () -> orderRepo.createFromCart(0, sampleAddressId, null, "COD", ""));
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("TEST 9: Customer Add Cart")
    public void testCustomerAddCart() throws SQLException {
        cartRepo.clear(customerA_id);
        assertEquals(0, cartRepo.count(customerA_id));

        cartRepo.add(customerA_id, sampleVariantId, 2);
        assertEquals(2, cartRepo.count(customerA_id));

        List<Map<String, Object>> items = cartRepo.items(customerA_id);
        assertEquals(1, items.size());
        assertEquals(sampleVariantId, items.get(0).get("variantId"));
        assertEquals(2, items.get(0).get("quantity"));
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("TEST 10: Customer Update Cart")
    public void testCustomerUpdateCart() throws SQLException {
        cartRepo.update(customerA_id, sampleVariantId, 3);
        assertEquals(3, cartRepo.count(customerA_id));
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    @DisplayName("TEST 11: Customer Remove Cart Item")
    public void testCustomerRemoveCartItem() throws SQLException {
        cartRepo.remove(customerA_id, sampleVariantId);
        assertEquals(0, cartRepo.count(customerA_id));

        // Re-add for checkout
        cartRepo.add(customerA_id, sampleVariantId, 1);
        assertEquals(1, cartRepo.count(customerA_id));
    }

    @Test
    @org.junit.jupiter.api.Order(10)
    @DisplayName("TEST 12: Customer Checkout preparation")
    public void testCustomerCheckout() throws SQLException {
        BigDecimal subtotal = cartRepo.subtotal(customerA_id);
        assertTrue(subtotal.compareTo(BigDecimal.ZERO) > 0);
        List<Map<String, Object>> addrs = addressRepo.findAll(customerA_id);
        assertFalse(addrs.isEmpty());
    }

    @Test
    @org.junit.jupiter.api.Order(11)
    @DisplayName("TEST 13: Invalid Address")
    public void testInvalidAddressRejected() {
        assertThrows(SQLException.class, () -> orderRepo.createFromCart(customerA_id, 999999, null, "COD", ""));
    }

    @Test
    @org.junit.jupiter.api.Order(12)
    @DisplayName("TEST 14: Invalid Voucher")
    public void testInvalidVoucherRejected() {
        assertThrows(SQLException.class, () -> orderRepo.createFromCart(customerA_id, sampleAddressId, "INVALID_VOUCHER_CODE_999", "COD", ""));
    }

    @Test
    @org.junit.jupiter.api.Order(13)
    @DisplayName("TEST 15-17: Create Order & Cart Cleared")
    public void testCreateOrderAndClearCart() throws SQLException {
        long orderId = orderRepo.createFromCart(customerA_id, sampleAddressId, null, "COD", "Test note");
        assertTrue(orderId > 0);
        createdOrderId = orderId;

        // Cart items must be cleared
        assertEquals(0, cartRepo.count(customerA_id));

        Order order = orderRepo.findById((int) orderId);
        assertNotNull(order);
        assertEquals("PENDING", order.getStatus().name());
        assertEquals(customerA_id, order.getUserId());
    }

    @Test
    @org.junit.jupiter.api.Order(14)
    @DisplayName("TEST 18: Customer xem Order")
    public void testCustomerViewOrder() {
        List<Order> orders = orderRepo.findByCustomerId(customerA_id);
        assertFalse(orders.isEmpty());
        assertTrue(orders.stream().anyMatch(o -> o.getId() == createdOrderId));
    }

    @Test
    @org.junit.jupiter.api.Order(15)
    @DisplayName("TEST 19: Customer A không xem Order B")
    public void testCustomerA_CannotAccess_OrderB() {
        List<Order> ordersOfB = orderRepo.findByCustomerId(customerB_id);
        for (Order o : ordersOfB) {
            assertNotEquals(customerA_id, o.getUserId());
        }
    }

    @Test
    @org.junit.jupiter.api.Order(16)
    @DisplayName("TEST 20-24: Review & Rating rules")
    public void testReviewAndRatingWorkflow() throws SQLException {
        // Reviewing a PENDING order must fail
        Review r1 = new Review();
        r1.setOrderId(createdOrderId);
        r1.setProductId(sampleProductId);
        r1.setUserId(customerA_id);
        r1.setRating(5);
        r1.setContent("Đồng hồ rất đẹp!");
        assertThrows(SQLException.class, () -> reviewRepo.createReview(r1));

        // Rating out of 1-5 must fail
        Review rBad = new Review();
        rBad.setOrderId(createdOrderId);
        rBad.setProductId(sampleProductId);
        rBad.setUserId(customerA_id);
        rBad.setRating(6);
        rBad.setContent("Đồng hồ rất đẹp!");
        assertThrows(IllegalArgumentException.class, () -> reviewRepo.createReview(rBad));

        // Set order to COMPLETED
        orderRepo.updateStatus((int) createdOrderId, "COMPLETED");

        // Now review must succeed
        assertTrue(reviewRepo.createReview(r1));

        // Duplicate review for same order & product must fail
        assertThrows(SQLException.class, () -> reviewRepo.createReview(r1));

        // Review must appear in product reviews
        List<Review> pReviews = reviewRepo.findByProductId(sampleProductId);
        assertTrue(pReviews.stream().anyMatch(r -> r.getOrderId() != null && r.getOrderId() == createdOrderId));

        // Product rating average & count updated in DB
        var prod = productRepo.findById(sampleProductId).orElseThrow();
        assertTrue(prod.getRatingCount() > 0);
        assertTrue(prod.getRatingAverage() >= 1.0 && prod.getRatingAverage() <= 5.0);
    }

    @Test
    @org.junit.jupiter.api.Order(17)
    @DisplayName("TEST 26: Customer Comment")
    public void testCustomerComment() throws SQLException {
        assertTrue(commentRepo.insert(sampleProductId, customerA_id, "Hàng này có sẵn tại showroom không shop?"));
        List<ProductComment> comments = commentRepo.findByProductId(sampleProductId);
        assertFalse(comments.isEmpty());
        assertTrue(comments.stream().anyMatch(c -> c.getContent().contains("showroom")));
    }

    @Test
    @org.junit.jupiter.api.Order(18)
    @DisplayName("TEST 28-29: Warranty Online & Offline workflows and Image support")
    public void testWarrantyRules() throws SQLException {
        WarrantyRepository warrantyRepo = new WarrantyRepository();

        // 1. Lấy thông tin sản phẩm trong đơn hàng đã hoàn thành
        List<Map<String, Object>> items = orderRepo.getOrderItems((int) createdOrderId);
        assertFalse(items.isEmpty(), "Order items must not be empty");
        String productName = (String) items.get(0).get("productName");
        assertNotNull(productName);

        // 2. Online Warranty Submission: Khách hàng gửi bảo hành cho đơn đã COMPLETED
        String proofImage = "/uploads/warranty/proof_test_01.jpg";
        assertTrue(warrantyRepo.insertOnlineWarranty(createdOrderId, productName, "Đồng hồ bị hấp hơi nước khi rửa tay", proofImage));

        // 3. Has active warranty check & Duplicate prevention
        assertTrue(warrantyRepo.hasActiveWarranty(createdOrderId, productName));
        assertThrows(SQLException.class, () -> warrantyRepo.insertOnlineWarranty(createdOrderId, productName, "Gửi lại lần nữa", proofImage));

        // 4. Verify saved online warranty details
        List<Map<String, Object>> orderWarranties = warrantyRepo.findByOrderId(createdOrderId);
        assertFalse(orderWarranties.isEmpty());
        Map<String, Object> onlineW = orderWarranties.get(0);
        assertEquals("ONLINE", onlineW.get("warrantyType"));
        assertEquals("Chờ tiếp nhận", onlineW.get("status"));
        assertEquals(proofImage, onlineW.get("imageUrl"));
        assertNotNull(onlineW.get("startDate"));
        assertNotNull(onlineW.get("endDate"));
        assertFalse((Boolean) onlineW.get("isExpired"));

        // 5. Offline Warranty Submission: Nhân viên lập phiếu bảo hành trực tiếp tại quầy
        String offlineCustName = "Khách Hàng Trực Tiếp Test";
        String offlinePhone = "0988776655";
        String offlineProduct = "Đồng Hồ Automatic Test";
        String offlineSerial = "AT-2026-9999";
        String offlineProof = "/uploads/warranty/offline_watch_proof.jpg";

        assertTrue(warrantyRepo.insertOfflineWarranty(offlineCustName, offlinePhone, "off@test.com",
                offlineProduct, offlineSerial, 24, null, "Kim giây bị kẹt", offlineProof));

        // 6. Verify Offline Warranty: Trạng thái tự động là 'Đang xử lý' (Không qua confirm thừa)
        List<Map<String, Object>> offlineList = warrantyRepo.search(offlinePhone, null);
        assertFalse(offlineList.isEmpty(), "Offline warranty must be found by phone");
        Map<String, Object> offW = offlineList.get(0);
        assertEquals("OFFLINE", offW.get("warrantyType"));
        assertEquals("Đang xử lý", offW.get("status"), "Offline warranty must start directly at 'Đang xử lý'");
        assertEquals(24, offW.get("months"));
        assertEquals(offlineProof, offW.get("imageUrl"));
        assertNull(offW.get("orderId"));

        // 7. Workflow progression: Cập nhật sửa chữa hoàn tất -> Đã trả khách
        int offWarrantyId = (int) offW.get("id");
        java.sql.Date today = java.sql.Date.valueOf(java.time.LocalDate.now());
        assertTrue(warrantyRepo.updateRepair(offWarrantyId, "Căn chỉnh lại bộ kim và lau dầu", "Không thay linh kiện", "Đã test 24h chạy chuẩn", today));
        assertEquals("Hoàn tất", warrantyRepo.findById(offWarrantyId).get("status"));

        assertTrue(warrantyRepo.updateReturn(offWarrantyId, today));
        assertEquals("Đã trả khách", warrantyRepo.findById(offWarrantyId).get("status"));
    }

    @Test
    @org.junit.jupiter.api.Order(19)
    @DisplayName("TEST 30-31: Cancel Order rules")
    public void testCancelOrderRules() throws SQLException {
        // Create another pending order for cancel testing
        cartRepo.add(customerA_id, sampleVariantId, 1);
        long cancelOrderId = orderRepo.createFromCart(customerA_id, sampleAddressId, null, "COD", "To be cancelled");

        // Customer B cannot cancel Customer A's order
        assertThrows(SQLException.class, () -> orderRepo.cancel(cancelOrderId, customerB_id, "Hủy nhầm"));

        // Customer A can cancel own PENDING order
        orderRepo.cancel(cancelOrderId, customerA_id, "Đổi ý muốn mua mẫu khác");
        Order cancelled = orderRepo.findById((int) cancelOrderId);
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());

        // Cancel an already cancelled/completed order must fail
        assertThrows(SQLException.class, () -> orderRepo.cancel(cancelOrderId, customerA_id, "Hủy lại"));
    }
}
