<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="currentPath" value="${pageContext.request.requestURI}" />

<aside class="portal-sidebar" data-sidebar>
    <div class="portal-sidebar-head">
        <a class="portal-logo" href="${cp}/page/home" aria-label="WatchStore">
            <span>W</span>
            <span><b>WATCHSTORE</b><small>MANAGEMENT SUITE</small></span>
        </a>
        <button type="button" data-sidebar-close aria-label="Đóng menu">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m6 6 12 12M18 6 6 18"/></svg>
        </button>
    </div>

    <div class="portal-user">
        <div>${empty sessionScope.user ? 'QT' : fn:toUpperCase(fn:substring(sessionScope.user.fullName, 0, 1))}</div>
        <span>
            <b>${empty sessionScope.user ? 'Quản trị viên' : sessionScope.user.fullName}</b>
            <small>${empty sessionScope.user ? 'ADMINISTRATOR' : sessionScope.user.role.label}</small>
        </span>
        <i aria-label="Đang trực tuyến"></i>
    </div>

    <nav aria-label="Menu quản trị">
        <span class="portal-nav-label">ĐIỀU HƯỚNG</span>
        <c:choose>
            <c:when test="${adminArea == 'sales'}">
                <a class="${fn:contains(currentPath, '/sales/dashboard') ? 'is-active' : ''}" href="${cp}/manage/sales/dashboard"><svg viewBox="0 0 24 24"><rect x="4" y="4" width="6" height="6"/><rect x="14" y="4" width="6" height="6"/><rect x="4" y="14" width="6" height="6"/><rect x="14" y="14" width="6" height="6"/></svg><span>Tổng quan</span></a>
                <a class="${fn:contains(currentPath, '/sales/orders') ? 'is-active' : ''}" href="${cp}/manage/sales/orders"><svg viewBox="0 0 24 24"><path d="M6 3h12v18H6zM9 8h6M9 12h6M9 16h4"/></svg><span>Đơn hàng</span></a>
                <a class="${fn:contains(currentPath, '/sales/customers') ? 'is-active' : ''}" href="${cp}/manage/sales/customers"><svg viewBox="0 0 24 24"><circle cx="9" cy="8" r="3"/><path d="M3 20a6 6 0 0 1 12 0M16 4a3 3 0 0 1 0 6M17 14a5 5 0 0 1 4 5"/></svg><span>Khách hàng</span></a>
                <a class="${fn:contains(currentPath, '/sales/reviews') ? 'is-active' : ''}" href="${cp}/manage/sales/reviews"><svg viewBox="0 0 24 24"><path d="m12 3 2.8 5.7 6.2.9-4.5 4.4 1.1 6.2-5.6-3-5.6 3 1.1-6.2L3 9.6l6.2-.9Z"/></svg><span>Đánh giá</span></a>
                <a class="${fn:contains(currentPath, '/sales/delivery') ? 'is-active' : ''}" href="${cp}/manage/sales/delivery"><svg viewBox="0 0 24 24"><path d="M3 6h11v11H3zM14 10h4l3 3v4h-7z"/><circle cx="7" cy="18" r="2"/><circle cx="18" cy="18" r="2"/></svg><span>Vận chuyển</span></a>
                <a class="${fn:contains(currentPath, '/sales/returns') ? 'is-active' : ''}" href="${cp}/manage/sales/returns"><svg viewBox="0 0 24 24"><path d="M4 8h11a5 5 0 0 1 0 10H9M4 8l4-4M4 8l4 4"/></svg><span>Đổi trả</span></a>
                <a class="${fn:contains(currentPath, '/sales/report') ? 'is-active' : ''}" href="${cp}/manage/sales/report"><svg viewBox="0 0 24 24"><path d="M5 20V10M12 20V4M19 20v-7"/></svg><span>Báo cáo</span></a>
            </c:when>

            <c:when test="${adminArea == 'warehouse'}">
                <a class="${fn:contains(currentPath, '/warehouse/dashboard') ? 'is-active' : ''}" href="${cp}/manage/warehouse/dashboard"><svg viewBox="0 0 24 24"><rect x="4" y="4" width="6" height="6"/><rect x="14" y="4" width="6" height="6"/><rect x="4" y="14" width="6" height="6"/><rect x="14" y="14" width="6" height="6"/></svg><span>Tổng quan</span></a>
                <a class="${fn:contains(currentPath, '/warehouse/receipts') ? 'is-active' : ''}" href="${cp}/manage/warehouse/receipts"><svg viewBox="0 0 24 24"><path d="M12 3v12M7 10l5 5 5-5M4 20h16"/></svg><span>Phiếu nhập</span></a>
                <a class="${fn:contains(currentPath, '/warehouse/exports') ? 'is-active' : ''}" href="${cp}/manage/warehouse/exports"><svg viewBox="0 0 24 24"><path d="M12 16V4M7 9l5-5 5 5M4 20h16"/></svg><span>Phiếu xuất</span></a>
                <a class="${fn:contains(currentPath, '/warehouse/inventory') ? 'is-active' : ''}" href="${cp}/manage/warehouse/inventory"><svg viewBox="0 0 24 24"><path d="m4 7 8-4 8 4-8 4zM4 7v10l8 4 8-4V7M12 11v10"/></svg><span>Tồn kho</span></a>
                <a class="${fn:contains(currentPath, '/warehouse/stocktake') ? 'is-active' : ''}" href="${cp}/manage/warehouse/stocktake"><svg viewBox="0 0 24 24"><path d="M7 3h10v4H7zM5 5H3v16h18V5h-2M8 13l3 3 5-6"/></svg><span>Kiểm kê</span></a>
                <a class="${fn:contains(currentPath, '/warehouse/variants') ? 'is-active' : ''}" href="${cp}/manage/warehouse/variants"><svg viewBox="0 0 24 24"><path d="m12 3 8 9-8 9-8-9z"/></svg><span>Biến thể</span></a>
                <a class="${fn:contains(currentPath, '/warehouse/alerts') ? 'is-active' : ''}" href="${cp}/manage/warehouse/alerts"><svg viewBox="0 0 24 24"><path d="M12 3 2 21h20zM12 9v5M12 18h.01"/></svg><span>Cảnh báo kho</span></a>
            </c:when>

            <c:otherwise>
                <a class="${fn:contains(currentPath, '/admin/dashboard') ? 'is-active' : ''}" href="${cp}/manage/admin/dashboard"><svg viewBox="0 0 24 24"><rect x="4" y="4" width="6" height="6"/><rect x="14" y="4" width="6" height="6"/><rect x="4" y="14" width="6" height="6"/><rect x="14" y="14" width="6" height="6"/></svg><span>Tổng quan</span></a>
                <span class="portal-nav-label portal-nav-label-spaced">HỆ THỐNG</span>
                <a class="${fn:contains(currentPath, '/admin/accounts') ? 'is-active' : ''}" href="${cp}/manage/admin/accounts"><svg viewBox="0 0 24 24"><circle cx="9" cy="8" r="3"/><path d="M3 20a6 6 0 0 1 12 0M16 4a3 3 0 0 1 0 6M17 14a5 5 0 0 1 4 5"/></svg><span>Tài khoản</span></a>
                <a class="${fn:contains(currentPath, '/admin/roles') ? 'is-active' : ''}" href="${cp}/manage/admin/roles"><svg viewBox="0 0 24 24"><path d="M12 3 5 6v5c0 5 3 8 7 10 4-2 7-5 7-10V6zM9 12l2 2 4-4"/></svg><span>Vai trò</span></a>
                <span class="portal-nav-label portal-nav-label-spaced">DANH MỤC BÁN HÀNG</span>
                <a class="${fn:contains(currentPath, '/admin/products') ? 'is-active' : ''}" href="${cp}/manage/admin/products"><svg viewBox="0 0 24 24"><path d="m4 7 8-4 8 4-8 4zM4 7v10l8 4 8-4V7M12 11v10"/></svg><span>Sản phẩm</span></a>
                <a class="${fn:contains(currentPath, '/admin/categories') ? 'is-active' : ''}" href="${cp}/manage/admin/categories"><svg viewBox="0 0 24 24"><path d="M4 4h6v6H4zM14 4h6v6h-6zM4 14h6v6H4zM14 14h6v6h-6z"/></svg><span>Danh mục</span></a>
                <a class="${fn:contains(currentPath, '/admin/brands') ? 'is-active' : ''}" href="${cp}/manage/admin/brands"><svg viewBox="0 0 24 24"><path d="m12 3 8 9-8 9-8-9z"/><circle cx="12" cy="12" r="2"/></svg><span>Thương hiệu</span></a>
                <a class="${fn:contains(currentPath, '/admin/vouchers') ? 'is-active' : ''}" href="${cp}/manage/admin/vouchers"><svg viewBox="0 0 24 24"><path d="M4 6h16v4a2 2 0 0 0 0 4v4H4v-4a2 2 0 0 0 0-4zM9 8v8"/></svg><span>Voucher</span></a>
                <a class="${fn:contains(currentPath, '/admin/posts') ? 'is-active' : ''}" href="${cp}/manage/admin/posts"><svg viewBox="0 0 24 24"><path d="M5 3h14v18H5zM8 7h8M8 11h8M8 15h5"/></svg><span>Bài viết</span></a>
                <a class="${fn:contains(currentPath, '/admin/statistics') ? 'is-active' : ''}" href="${cp}/manage/admin/statistics"><svg viewBox="0 0 24 24"><path d="M5 20V10M12 20V4M19 20v-7"/></svg><span>Thống kê</span></a>
            </c:otherwise>
        </c:choose>
    </nav>

    <div class="portal-sidebar-footer">
        <a href="${cp}/page/home"><svg viewBox="0 0 24 24"><path d="M3 11 12 3l9 8M5 10v11h14V10M9 21v-7h6v7"/></svg><span>Về cửa hàng</span></a>
        <a class="logout-link" href="${cp}/auth/logout"><svg viewBox="0 0 24 24"><path d="M10 4H4v16h6M14 8l4 4-4 4M8 12h10"/></svg><span>Đăng xuất</span></a>
    </div>
</aside>
