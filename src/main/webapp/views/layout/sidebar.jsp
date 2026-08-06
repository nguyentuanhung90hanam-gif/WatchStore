<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<aside class="portal-sidebar" data-sidebar>
    <a class="portal-logo" href="${cp}/page/home"><span>W</span><b>WATCHSTORE</b></a>
    <div class="portal-user"><div>${empty sessionScope.user ? 'TN' : fn:substring(sessionScope.user.fullName, 0, 1)}</div><span><b>${empty sessionScope.user ? 'Quản trị viên' : sessionScope.user.fullName}</b><small>${empty sessionScope.user ? 'ADMIN' : sessionScope.user.role.label}</small></span></div>
    <nav>
        <c:choose>
            <c:when test="${adminArea == 'sales'}">
                <a href="${cp}/manage/sales/dashboard">▦ Tổng quan</a><a href="${cp}/manage/sales/orders">▣ Đơn hàng</a><a href="${cp}/manage/sales/customers">♙ Khách hàng</a><a href="${cp}/manage/sales/reviews">★ Đánh giá</a><a href="${cp}/manage/sales/delivery">▤ Vận chuyển</a><a href="${cp}/manage/sales/returns">↺ Đổi trả</a><a href="${cp}/manage/sales/report">◫ Báo cáo</a>
            </c:when>
            <c:when test="${adminArea == 'warehouse'}">
                <div class="sidebar-group">
                    <div class="sidebar-group-title">TỔNG QUAN</div>
                    <a href="${cp}/manage/warehouse/dashboard">▦ Tổng quan</a>
                </div>
                <div class="sidebar-group">
                    <div class="sidebar-group-title">QUẢN LÝ KHO</div>
                    <a href="${cp}/manage/warehouse/receipts">⇩ Phiếu nhập</a>
                    <a href="${cp}/manage/warehouse/exports">⇧ Phiếu xuất</a>
                    <a href="${cp}/manage/warehouse/inventory">▣ Tồn kho</a>
                    <a href="${cp}/manage/warehouse/stocktake">✓ Kiểm kê</a>
                    <a href="${cp}/manage/warehouse/variants">◇ Biến thể</a>
                    <a href="${cp}/manage/warehouse/alerts">⚠ Cảnh báo</a>
                </div>
                <div class="sidebar-group">
                    <div class="sidebar-group-title">THỐNG KÊ</div>
                    <a href="${cp}/manage/warehouse/statistics">◫ Báo cáo kho</a>
                </div>
                <div class="sidebar-group">
                    <div class="sidebar-group-title">TÀI KHOẢN</div>
                    <a href="${cp}/page/profile">♙ Thông tin cá nhân</a>
                    <a href="${cp}/page/change-password">🔑 Đổi mật khẩu</a>
                </div>
            </c:when>
            <c:otherwise>
                <a href="${cp}/manage/admin/dashboard">▦ Tổng quan</a><a href="${cp}/manage/admin/accounts">♙ Tài khoản</a><a href="${cp}/manage/admin/roles">♜ Vai trò</a><a href="${cp}/manage/admin/products">▣ Sản phẩm</a><a href="${cp}/manage/admin/categories">⌘ Danh mục</a><a href="${cp}/manage/admin/brands">◆ Thương hiệu</a><a href="${cp}/manage/admin/vouchers">% Voucher</a><a href="${cp}/manage/admin/posts">▤ Bài viết</a><a href="${cp}/manage/admin/statistics">◫ Thống kê</a>
            </c:otherwise>
        </c:choose>
    </nav>
    <a class="logout-link" href="${cp}/auth/logout">↪ Đăng xuất</a>
</aside>
