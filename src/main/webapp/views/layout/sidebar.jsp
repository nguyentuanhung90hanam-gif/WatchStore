<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<aside class="portal-sidebar" data-sidebar>

    <a class="portal-logo" href="${cp}/page/home">
        <span>W</span>
        <b>WATCHSTORE</b>
    </a>

    <div class="portal-user">
        <div>
            ${empty sessionScope.user ? 'TN' : fn:substring(sessionScope.user.fullName, 0, 1)}
        </div>
        <span>
            <b>
                ${empty sessionScope.user ? 'Quản trị viên' : sessionScope.user.fullName}
            </b>
            <small>
                ${empty sessionScope.user ? 'ADMIN' : sessionScope.user.role.label}
            </small>
        </span>
    </div>

    <nav>
        <c:choose>
            <%-- MODE 1: ADMIN (SUPER ADMIN - TOÀN BỘ MENU) --%>
            <c:when test="${sessionScope.user.role == 'ADMIN'}">
                <div class="sidebar-section-title" style="padding:8px 12px 2px; font-size:11px; font-weight:700; color:#9ca3af; text-transform:uppercase; letter-spacing:0.5px;">
                    HỆ THỐNG
                </div>
                <a href="${cp}/manage/admin/dashboard">Tổng quan Admin</a>
                <a href="${cp}/manage/admin/accounts">Quản lý tài khoản</a>
                <a href="${cp}/manage/admin/roles">Quản lý vai trò</a>
                <a href="${cp}/manage/admin/permissions">Phân quyền Nhân viên</a>

                <div class="sidebar-section-title" style="padding:10px 12px 2px; font-size:11px; font-weight:700; color:#9ca3af; text-transform:uppercase; letter-spacing:0.5px;">
                    SẢN PHẨM & NỘI DUNG
                </div>
                <a href="${cp}/manage/admin/products">Quản lý sản phẩm</a>
                <a href="${cp}/manage/admin/categories">Danh mục sản phẩm</a>
                <a href="${cp}/manage/admin/brands">Thương hiệu đồng hồ</a>
                <a href="${cp}/manage/admin/vouchers">Mã giảm giá (Voucher)</a>
                <a href="${cp}/manage/admin/banners">Banner khuyến mãi</a>
                <a href="${cp}/manage/admin/posts">Bài viết tin tức</a>

                <div class="sidebar-section-title" style="padding:10px 12px 2px; font-size:11px; font-weight:700; color:#9ca3af; text-transform:uppercase; letter-spacing:0.5px;">
                    BÁN HÀNG
                </div>
                <a href="${cp}/manage/sales/orders">Quản lý đơn hàng</a>
                <a href="${cp}/manage/sales/customers">Quản lý khách hàng</a>
                <a href="${cp}/manage/sales/warranty">Quản lý bảo hành</a>
                <a href="${cp}/manage/admin/statistics">Thống kê hệ thống</a>
                <a href="${cp}/manage/admin/reports">Báo cáo doanh thu</a>
            </c:when>

            <%-- MODE 2: EMPLOYEE (HIỂN THỊ CÁC MENU CÓ QUYỀN VIEW TƯƠNG ỨNG) --%>
            <c:when test="${sessionScope.user.role == 'EMPLOYEE'}">
                <%-- SẢN PHẨM --%>
                <c:if test="${sessionScope.userPermissions.contains('PRODUCT_VIEW')}">
                    <a href="${cp}/manage/admin/products">Sản phẩm</a>
                </c:if>

                <%-- ĐƠN HÀNG --%>
                <c:if test="${sessionScope.userPermissions.contains('ORDER_VIEW') || sessionScope.userPermissions.contains('SALES_ORDER')}">
                    <a href="${cp}/manage/sales/orders">Đơn hàng</a>
                </c:if>

                <%-- KHÁCH HÀNG --%>
                <c:if test="${sessionScope.userPermissions.contains('CUSTOMER_VIEW') || sessionScope.userPermissions.contains('SALES_CUSTOMER')}">
                    <a href="${cp}/manage/sales/customers">Khách hàng</a>
                </c:if>

                <%-- BẢO HÀNH --%>
                <c:if test="${sessionScope.userPermissions.contains('SALES_WARRANTY') || sessionScope.userPermissions.contains('ORDER_VIEW')}">
                    <a href="${cp}/manage/sales/warranty">Bảo hành</a>
                </c:if>

                <%-- VOUCHER --%>
                <c:if test="${sessionScope.userPermissions.contains('VOUCHER_VIEW')}">
                    <a href="${cp}/manage/admin/vouchers">Mã giảm giá (Voucher)</a>
                </c:if>

                <%-- BÁO CÁO --%>
                <c:if test="${sessionScope.userPermissions.contains('REPORT_VIEW') || sessionScope.userPermissions.contains('SALES_REPORT') || sessionScope.userPermissions.contains('REPORT_EXPORT')}">
                    <a href="${cp}/manage/sales/report">Báo cáo & Thống kê</a>
                </c:if>
            </c:when>

            <%-- MODE 3: OTHER (CUSTOMER / GUEST) --%>
            <c:otherwise>
            </c:otherwise>
        </c:choose>
    </nav>

    <a class="logout-link" href="${cp}/auth/logout">
        Đăng xuất
    </a>

</aside>