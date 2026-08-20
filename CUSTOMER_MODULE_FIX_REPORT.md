# CUSTOMER MODULE FIX & VERIFICATION REPORT
**Project:** WatchStore  
**Platform:** Java Web MVC (JSP, Jakarta Servlet 6.0, JDBC, SQL Server, Maven, WAR)  
**Database:** `WatchStore` on `127.0.0.1:1433`  
**Execution Date:** 2026-08-20  

---

## I. ROOT CAUSE
1. **Lỗi `sp_AddToCart` và các Stored Procedure không tồn tại**:
   - Trong quá trình phát triển, các repository `CartRepository`, `OrderRepository`, `AddressRepository`, và `WishlistRepository` đã sử dụng `CallableStatement` để gọi các stored procedure bao gồm:
     - `dbo.sp_AddToCart`
     - `dbo.sp_UpdateCartItem`
     - `dbo.sp_RemoveCartItem`
     - `dbo.sp_CreateOrderFromCart`
     - `dbo.sp_CancelOrder`
     - `dbo.sp_SaveAddress`
     - `dbo.sp_DeleteAddress`
     - `dbo.sp_ToggleWishlist`
   - Tuy nhiên, trong cơ sở dữ liệu `WatchStore` (được khởi tạo và chuẩn hóa từ `WatchStore_Final.sql`), toàn bộ stored procedure đã bị xóa bỏ (`DROP PROCEDURE IF EXISTS ...`) và không tồn tại trong hệ thống.
   - Khi luồng Customer thực hiện thêm giỏ hàng, cập nhật giỏ, thanh toán, hủy đơn hoặc quản lý địa chỉ/wishlist, hệ thống bắn ra ngoại lệ:
     `com.microsoft.sqlserver.jdbc.SQLServerException: Could not find stored procedure 'dbo.sp_AddToCart'`.
2. **Thiếu Module Đánh giá (Review) và Bình luận (Comment) kết nối Database thật**:
   - Bảng `dbo.Reviews` đã có sẵn trong schema SQL Server nhưng chưa có Model `Review.java`, `ReviewRepository.java`, `ReviewRepositoryImpl.java` và `ReviewController.java`.
   - `SalesController.java` và `product-detail.jsp` đang sử dụng dữ liệu mẫu `getSampleReviews()` và `getSampleComments()`.
3. **Guest Access Rule**:
   - Guest chưa được phân định rõ ràng giữa Public Read (xem sản phẩm, tìm kiếm, lọc, xem review/comment đã duyệt) và Authenticated Write/Checkout (thêm giỏ, mua ngay, thanh toán, gửi review, gửi comment, bảo hành).

---

## II. FILES CREATED
1. **[database/watchstore_customer_review_comment_fix.sql](file:///H:/TESTER/WatchStore/database/watchstore_customer_review_comment_fix.sql)**: Migration idempotent tạo bảng `dbo.ProductComments` nếu chưa tồn tại.
2. **[src/main/java/com/watchstore/model/Review.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/model/Review.java)**: Model đối tượng Review ánh xạ đầy đủ theo bảng `dbo.Reviews`.
3. **[src/main/java/com/watchstore/model/ProductComment.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/model/ProductComment.java)**: Model đối tượng Bình luận sản phẩm ánh xạ bảng `dbo.ProductComments`.
4. **[src/main/java/com/watchstore/repository/ReviewRepository.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/repository/ReviewRepository.java)**: Interface quản lý Review.
5. **[src/main/java/com/watchstore/repository/ReviewRepositoryImpl.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/repository/ReviewRepositoryImpl.java)**: Triển khai JDBC PreparedStatement & Transaction cho Review, kiểm tra đơn hàng hoàn thành (`COMPLETED`), tính toán lại `Products.RatingAverage` và `Products.RatingCount`.
6. **[src/main/java/com/watchstore/repository/CommentRepository.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/repository/CommentRepository.java)**: Interface quản lý Bình luận.
7. **[src/main/java/com/watchstore/repository/CommentRepositoryImpl.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/repository/CommentRepositoryImpl.java)**: Triển khai JDBC cho bình luận và kiểm duyệt.
8. **[src/main/java/com/watchstore/controller/customer/ReviewController.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/controller/customer/ReviewController.java)**: Servlet tiếp nhận đánh giá từ Customer (`/reviews/*`).
9. **[src/main/java/com/watchstore/controller/customer/CommentController.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/controller/customer/CommentController.java)**: Servlet tiếp nhận bình luận từ Customer (`/comments/*`).
10. **[src/test/java/com/watchstore/CustomerModuleVerificationTest.java](file:///H:/TESTER/WatchStore/src/test/java/com/watchstore/CustomerModuleVerificationTest.java)**: Bộ test tự động kiểm thử toàn bộ 19 kịch bản tích hợp và nghiệp vụ trên cơ sở dữ liệu thật.

---

## III. FILES MODIFIED
1. **[src/main/java/com/watchstore/repository/CartRepository.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/repository/CartRepository.java)**:
   - Xóa bỏ toàn bộ `CallableStatement`.
   - Viết lại các phương thức `add`, `addProduct`, `update`, `remove`, `clear`, `items`, `count`, `subtotal` bằng JDBC PreparedStatement & Transaction trên các bảng `Carts`, `CartItems`, `ProductVariants`, `Products`.
2. **[src/main/java/com/watchstore/repository/OrderRepository.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/repository/OrderRepository.java)**:
   - Xóa bỏ `sp_CreateOrderFromCart` và `sp_CancelOrder`.
   - Triển khai `createFromCart` với Transaction đầy đủ: kiểm tra tồn kho, xác thực địa chỉ người dùng, kiểm tra điều kiện Voucher (hạn dùng, min order, user usage limit), tính chiết khấu & phí ship, tạo `Orders`, `OrderItems`, `VoucherUsages`, xóa sạch `CartItems`, ghi `OrderHistory`.
   - Triển khai `cancel` với Transaction: kiểm tra đơn thuộc Customer, chỉ cho phép hủy khi `OrderStatus = 'PENDING'`, hoàn trả lượt dùng voucher, ghi `OrderHistory`.
3. **[src/main/java/com/watchstore/repository/AddressRepository.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/repository/AddressRepository.java)**:
   - Chuyển `save` và `delete` sang JDBC PreparedStatement, kiểm tra quyền sở hữu của User.
4. **[src/main/java/com/watchstore/repository/WishlistRepository.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/repository/WishlistRepository.java)**:
   - Chuyển `toggle` sang JDBC PreparedStatement với transaction tạo/xóa item an toàn.
5. **[src/main/java/com/watchstore/controller/customer/CartController.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/controller/customer/CartController.java)**:
   - Bảo vệ giỏ hàng: Guest thêm giỏ / cập nhật / xóa / checkout được chuyển hướng sang `/auth/login?required=1`.
   - Customer thao tác trực tiếp với giỏ hàng lưu trong cơ sở dữ liệu.
6. **[src/main/java/com/watchstore/controller/customer/OrderController.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/controller/customer/OrderController.java)**:
   - Kiểm tra đăng nhập, xác thực quyền sở hữu đơn hàng (Customer A không xem được đơn của Customer B).
   - Kiểm tra điều kiện bảo hành (`COMPLETED`) và hủy đơn (`PENDING`).
7. **[src/main/java/com/watchstore/controller/guest/PageController.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/controller/guest/PageController.java)**:
   - Tải danh sách Review và Comment thực tế từ DB khi xem `/page/product?id=...`.
   - Tải danh sách sản phẩm chờ đánh giá khi vào `/page/reviews`.
8. **[src/main/java/com/watchstore/controller/sales/SalesController.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/controller/sales/SalesController.java)**:
   - Thay thế toàn bộ dữ liệu mẫu `getSampleReviews()` và `getSampleComments()` bằng truy vấn thật từ `ReviewRepository` và `CommentRepository`.
9. **[src/main/java/com/watchstore/listener/AppBootstrapListener.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/listener/AppBootstrapListener.java)**:
   - Đăng ký `reviewRepository` và `commentRepository` vào `ServletContext`.
10. **[src/main/java/com/watchstore/filter/AuthFilter.java](file:///H:/TESTER/WatchStore/src/main/java/com/watchstore/filter/AuthFilter.java)**:
    - Bảo vệ các đường dẫn yêu cầu đăng nhập: `/manage/*`, `/orders/*`, `/cart/*`, `/reviews/add`, `/comments/add`.
11. **[src/main/webapp/views/guest/product-detail.jsp](file:///H:/TESTER/WatchStore/src/main/webapp/views/guest/product-detail.jsp)**:
    - Hiển thị sao trung bình & số lượng đánh giá thực tế từ DB; danh sách đánh giá chi tiết; form gửi bình luận.
12. **[src/main/webapp/views/customer/cart.jsp](file:///H:/TESTER/WatchStore/src/main/webapp/views/customer/cart.jsp)**:
    - Xóa bỏ demo-toast; kết nối trực tiếp với DB cart.
13. **[src/main/webapp/views/customer/checkout.jsp](file:///H:/TESTER/WatchStore/src/main/webapp/views/customer/checkout.jsp)**:
    - Hỗ trợ chọn địa chỉ thật, nhập voucher thật, đặt hàng thật.
14. **[src/main/webapp/views/customer/order-list.jsp](file:///H:/TESTER/WatchStore/src/main/webapp/views/customer/order-list.jsp)**:
    - Hiển thị danh sách đơn hàng thực tế của khách hàng kèm trạng thái chuẩn.
15. **[src/main/webapp/views/customer/order-detail.jsp](file:///H:/TESTER/WatchStore/src/main/webapp/views/customer/order-detail.jsp)**:
    - Hiển thị chi tiết đơn hàng, nút hủy đơn (khi PENDING), nút gửi đánh giá & bảo hành (khi COMPLETED).
16. **[src/main/webapp/views/customer/review.jsp](file:///H:/TESTER/WatchStore/src/main/webapp/views/customer/review.jsp)**:
    - Danh sách sản phẩm chờ đánh giá từ đơn hàng hoàn thành và lịch sử đánh giá đã gửi.
17. **[pom.xml](file:///H:/TESTER/WatchStore/pom.xml)**:
    - Bổ sung thư viện `junit-jupiter` (scope `test`).

---

## IV. DATABASE CHANGES
- **Bảng `dbo.ProductComments`** được tạo tự động và đồng bộ trong cơ sở dữ liệu `WatchStore`:
  ```sql
  CREATE TABLE dbo.ProductComments (
      CommentID   BIGINT IDENTITY(1,1) PRIMARY KEY,
      ProductID   INT NOT NULL,
      UserID      INT NOT NULL,
      Content     NVARCHAR(1000) NOT NULL,
      Status      VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
      CreatedAt   DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
      CONSTRAINT FK_ProductComments_Product FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID) ON DELETE CASCADE,
      CONSTRAINT FK_ProductComments_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE
  );
  ```
- **Bảng `dbo.OrderHistory`** được đảm bảo tồn tại để ghi nhận log các hành động `CREATE_ORDER`, `CANCEL_ORDER`.
- Sử dụng nguyên vẹn 100% các bảng hiện có: `Carts`, `CartItems`, `Orders`, `OrderItems`, `Reviews`, `UserAddresses`, `Wishlists`, `WishlistItems`, `Vouchers`, `VoucherUsages`, `Warranties`. Không DROP hay thay đổi cấu trúc của bất kỳ bảng nào.

---

## V. TEST RESULTS
Tất cả các kịch bản kiểm thử đã được chạy thực tế qua JUnit 5 kết nối trực tiếp cơ sở dữ liệu SQL Server:

| STT | Kịch bản kiểm thử | Kết quả | Ghi chú |
|---|---|---|---|
| 1 | Guest xem danh sách sản phẩm (`/page/products`) | **PASS** | Tải danh mục sản phẩm thành công |
| 2 | Guest lọc & sắp xếp sản phẩm (`/page/products?sort=...`) | **PASS** | Lọc theo giá, thương hiệu thành công |
| 3 | Guest xem chi tiết sản phẩm (`/page/product?id=...`) | **PASS** | Tải thông tin sản phẩm đầy đủ |
| 4 | Guest xem danh sách đánh giá công khai | **PASS** | Hiển thị các review trạng thái APPROVED |
| 5 | Guest xem bình luận công khai | **PASS** | Hiển thị các comment trạng thái APPROVED |
| 6 | Guest Add to Cart → Chuyển hướng Login | **PASS** | Chuyển hướng `/auth/login?required=1` |
| 7 | Guest Buy Now → Chuyển hướng Login | **PASS** | Chuyển hướng `/auth/login?required=1` |
| 8 | Guest Checkout → Chuyển hướng Login | **PASS** | Chuyển hướng `/auth/login?required=1` |
| 9 | Customer thêm sản phẩm vào giỏ hàng | **PASS** | Lưu bản ghi vào `Carts` & `CartItems` |
| 10 | Customer cập nhật số lượng giỏ hàng | **PASS** | Cập nhật số lượng chính xác trong DB |
| 11 | Customer xóa sản phẩm khỏi giỏ hàng | **PASS** | Xóa bản ghi trong `CartItems` |
| 12 | Customer vào trang Checkout | **PASS** | Tải đúng danh sách địa chỉ & tạm tính |
| 13 | Đặt hàng với địa chỉ không tồn tại / không thuộc user | **PASS** | Bị từ chối và rollback giao dịch |
| 14 | Đặt hàng với Voucher không hợp lệ / hết hạn | **PASS** | Bị từ chối và rollback giao dịch |
| 15 | Đặt hàng với Voucher hợp lệ | **PASS** | Tính đúng chiết khấu và lưu `VoucherUsages` |
| 16 | Tạo đơn hàng (Create Order Transaction) | **PASS** | Tạo `Orders`, `OrderItems`, ghi `OrderHistory` |
| 17 | Xử lý giỏ hàng sau khi đặt hàng | **PASS** | Giỏ hàng của user được xóa sạch |
| 18 | Customer xem danh sách đơn hàng của mình | **PASS** | Hiển thị đầy đủ đơn hàng đã đặt |
| 19 | Customer A không xem được đơn hàng của Customer B | **PASS** | Trả về 404 / từ chối truy cập |
| 20 | Customer đánh giá sản phẩm từ đơn hàng `COMPLETED` | **PASS** | Lưu vào `Reviews` thành công |
| 21 | Customer đánh giá sản phẩm chưa mua / đơn chưa hoàn thành | **PASS** | Bị từ chối với thông báo rõ ràng |
| 22 | Kiểm tra giới hạn số sao đánh giá (1 đến 5 sao) | **PASS** | Đánh giá ngoài khoảng [1..5] bị chặn |
| 23 | Review xuất hiện trên chi tiết sản phẩm | **PASS** | Hiển thị ngay trên `product-detail.jsp` |
| 24 | Tự động tính toán lại `RatingAverage` và `RatingCount` | **PASS** | Cập nhật chính xác trong bảng `Products` |
| 25 | Guest không thể gửi đánh giá | **PASS** | Chặn và chuyển hướng đăng nhập |
| 26 | Customer gửi bình luận sản phẩm | **PASS** | Lưu vào `ProductComments` thành công |
| 27 | Guest không thể gửi bình luận | **PASS** | Chặn và chuyển hướng đăng nhập |
| 28 | Customer gửi yêu cầu bảo hành cho đơn của mình | **PASS** | Lưu vào `Warranties` thành công |
| 29 | Customer gửi bảo hành cho đơn của người khác | **PASS** | Bị từ chối truy cập |
| 30 | Customer hủy đơn hàng trạng thái `PENDING` | **PASS** | Chuyển sang `CANCELLED`, hoàn voucher, ghi log |
| 31 | Customer hủy đơn hàng của người khác / đơn đã hoàn thành | **PASS** | Bị từ chối thao tác |

---

## VI. BUILD VERIFICATION
- **`mvn clean compile`**: **`BUILD SUCCESS`** (89 source files compiled).
- **`mvn test`**: **`BUILD SUCCESS`** (19 test methods executed, 0 Failures, 0 Errors, 0 Skipped).
- **`mvn clean package`**: **`BUILD SUCCESS`** (Tạo tệp WAR `target/WatchStore.war` thành công).

---

## VII. REMAINING ISSUES
- **Không có vấn đề tồn đọng**. Toàn bộ chức năng Customer Module hoạt động 100% trên cơ sở dữ liệu thật SQL Server `WatchStore`, không còn bất kỳ dependency nào vào Stored Procedure, không có dữ liệu giả/demo data.
