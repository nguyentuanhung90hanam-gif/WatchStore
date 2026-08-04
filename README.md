# WatchStore — Java Web MVC/JSP

WatchStore là dự án website thương mại điện tử bán đồng hồ thời trang nam. Giao diện storefront đã được thiết kế lại hoàn chỉnh theo phong cách Modern Luxury với màu đen, trắng kem, đỏ burgundy và vàng đồng.

## Công nghệ

- Java 17
- Jakarta Servlet 6
- JSP, JSTL 3 và Expression Language
- Maven WAR
- HTML5, CSS3 và JavaScript thuần
- SQL Server JDBC đã khai báo sẵn để phát triển database sau

## Chạy dự án

1. Mở thư mục `WatchStore` bằng IntelliJ IDEA.
2. Chờ Maven tải dependency trong `pom.xml`.
3. Cấu hình Tomcat 10.1 trở lên.
4. Deploy artifact `WatchStore:war exploded`.
5. Mở `http://localhost:8080/WatchStore/`.

Có thể build thủ công:

```bash
mvn clean package
```

File WAR được tạo tại `target/WatchStore.war`.

Nếu `http://localhost:8080/WatchStore/page/home` báo 404 khi chạy bằng IntelliJ, kiểm tra cấu hình Tomcat và đặt **Application context** của artifact thành `/WatchStore`. Dự án cần Tomcat 10.1+ vì đang sử dụng package `jakarta.servlet` (Tomcat 9 trở xuống không tương thích).

CSS và JavaScript có mã phiên bản trên URL, đồng thời tài nguyên trong `/assets/` được đặt `no-cache` ở môi trường hiện tại. Cấu hình này tránh trường hợp Tomcat đã tải JSP mới nhưng trình duyệt vẫn giữ CSS cũ làm vỡ bố cục.

## Tài khoản demo

Mật khẩu có thể nhập bất kỳ giá trị nào từ 6 ký tự:

| Vai trò | Email |
|---|---|
| Quản trị viên | `admin@watchstore.vn` |
| Nhân viên bán hàng | `sales@watchstore.vn` |
| Nhân viên kho | `warehouse@watchstore.vn` |
| Khách hàng | `customer@watchstore.vn` |

## Dữ liệu hiện tại

Dự án chưa bắt buộc database. Sản phẩm và đơn hàng đang được cung cấp bởi:

- `MockProductRepository.java`
- `MockDataStore.java`
- Giỏ hàng lưu trong `HttpSession`

Khi phát triển SQL Server, giữ nguyên interface `ProductRepository` và tạo repository mới sử dụng `DBContext.getConnection()`. Controller và JSP không cần thay đổi cấu trúc.

Các biến môi trường database đã hỗ trợ:

```text
WATCHSTORE_DB_URL
WATCHSTORE_DB_USER
WATCHSTORE_DB_PASSWORD
```

## Phân vùng chức năng

- Guest: trang chủ, danh sách và chi tiết sản phẩm, tin tức, voucher, đăng nhập, đăng ký.
- Member/Customer: hồ sơ, địa chỉ, giỏ hàng, thanh toán, đơn hàng, yêu thích, đánh giá và thông báo.
- Sales: dashboard, đơn hàng, khách hàng, đánh giá, vận chuyển, đổi trả và báo cáo.
- Warehouse: phiếu nhập/xuất, tồn kho, kiểm kê, biến thể và cảnh báo.
- Admin: tài khoản, vai trò, phân quyền, danh mục, thương hiệu, sản phẩm, voucher, banner, bài viết, thông báo, thống kê và báo cáo.

## Kiến trúc

```text
Controller → Repository → Model
     ↓
JSP Layout + JSTL/EL
```

Không có truy vấn SQL hoặc Java Scriptlet trong các trang JSP.

## Giao diện WatchStore Pro

Thiết kế mới đã được tích hợp trực tiếp vào cấu trúc Java Web MVC/JSP hiện có, không phải một trang HTML rời. Controller, Repository, Model, phân quyền, giỏ hàng và toàn bộ URL cũ được giữ nguyên.

- Header hai tầng, thanh ưu đãi, tìm kiếm, tài khoản, yêu thích và giỏ hàng động.
- Menu chính có mega menu cho bộ máy, phong cách và thương hiệu.
- Trang chủ gồm hero, cam kết dịch vụ, thương hiệu, danh mục editorial, sản phẩm động, câu chuyện thương hiệu, ưu đãi đếm ngược, tạp chí, showroom và newsletter.
- Header, footer, nút, font, màu và các khối nội dung được đồng bộ cho toàn bộ trang Guest/Member/Customer.
- Giao diện responsive cho desktop, tablet và điện thoại; menu mobile có thể đóng bằng phím `Esc`.

## Banner trang chủ

Trang chủ đã tích hợp carousel 3 banner sử dụng ảnh cục bộ trong `assets/images`:

- Tự động chuyển sau 5,5 giây và chạy lặp liên tục.
- Có nút trước/sau, chấm điều hướng, phím mũi tên và thao tác vuốt trên điện thoại.
- Tạm dừng khi rê chuột hoặc focus vào banner; tự chạy tiếp khi rời khỏi banner.
- Thời gian tự chạy nằm tại thuộc tính `data-autoplay="5500"` trong `views/guest/home.jsp`.
- Muốn thêm banner, chỉ cần sao chép một khối `<article class="hero-slide">`; JavaScript sẽ tự tạo thêm chấm điều hướng và cập nhật số lượng.

Font giao diện sử dụng Noto Sans và Noto Serif để hỗ trợ đầy đủ tiếng Việt, đồng thời có Segoe UI, Arial và Georgia làm font dự phòng.
