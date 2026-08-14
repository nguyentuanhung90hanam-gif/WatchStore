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

        <c:if test="${sessionScope.user.hasPermission('DASHBOARD_VIEW')}">
            <div style="margin-top: 15px; padding: 8px 12px 4px 12px; font-size: 11px; font-weight: 700; color: #888; text-transform: uppercase; letter-spacing: 0.5px;">
                TỔNG QUAN
            </div>
            <a href="${cp}/manage/dashboard">Dashboard</a>
        </c:if>

        <c:if test="${sessionScope.user.hasAnyPermission('ORDERS_VIEW', 'ORDERS_MANAGE', 'CUSTOMERS_VIEW', 'CUSTOMERS_MANAGE', 'DELIVERY_VIEW', 'RETURNS_VIEW', 'RETURNS_MANAGE', 'WARRANTY_VIEW')}">
            <div style="margin-top: 15px; padding: 8px 12px 4px 12px; font-size: 11px; font-weight: 700; color: #888; text-transform: uppercase; letter-spacing: 0.5px;">
                BÁN HÀNG
            </div>
            <c:if test="${sessionScope.user.hasAnyPermission('ORDERS_VIEW', 'ORDERS_MANAGE')}">
                <a href="${cp}/manage/sales/orders">Đơn hàng</a>
            </c:if>
            <c:if test="${sessionScope.user.hasAnyPermission('CUSTOMERS_VIEW', 'CUSTOMERS_MANAGE')}">
                <a href="${cp}/manage/sales/customers">Khách hàng</a>
            </c:if>
            <c:if test="${sessionScope.user.hasPermission('DELIVERY_VIEW')}">
                <a href="${cp}/manage/sales/delivery">Giao hàng</a>
            </c:if>
            <c:if test="${sessionScope.user.hasAnyPermission('RETURNS_VIEW', 'RETURNS_MANAGE')}">
                <a href="${cp}/manage/sales/returns">Đổi trả</a>
            </c:if>
            <c:if test="${sessionScope.user.hasPermission('WARRANTY_VIEW')}">
                <a href="${cp}/manage/sales/warranty">Bảo hành</a>
            </c:if>
        </c:if>

        <c:if test="${sessionScope.user.hasAnyPermission('INVENTORY_VIEW', 'INVENTORY_MANAGE', 'STOCK_RECEIPT_VIEW', 'STOCK_RECEIPT_MANAGE', 'STOCK_EXPORT_VIEW', 'STOCK_EXPORT_MANAGE', 'STOCKTAKE_VIEW', 'STOCKTAKE_MANAGE', 'VARIANTS_VIEW', 'VARIANTS_MANAGE', 'SUPPLIERS_VIEW')}">
            <div style="margin-top: 15px; padding: 8px 12px 4px 12px; font-size: 11px; font-weight: 700; color: #888; text-transform: uppercase; letter-spacing: 0.5px;">
                KHO HÀNG
            </div>
            <c:if test="${sessionScope.user.hasAnyPermission('INVENTORY_VIEW', 'INVENTORY_MANAGE')}">
                <a href="${cp}/manage/warehouse/inventory">Tồn kho</a>
            </c:if>
            <c:if test="${sessionScope.user.hasAnyPermission('STOCK_RECEIPT_VIEW', 'STOCK_RECEIPT_MANAGE')}">
                <a href="${cp}/manage/warehouse/receipts">Phiếu nhập</a>
            </c:if>
            <c:if test="${sessionScope.user.hasAnyPermission('STOCK_EXPORT_VIEW', 'STOCK_EXPORT_MANAGE')}">
                <a href="${cp}/manage/warehouse/exports">Phiếu xuất</a>
            </c:if>
            <c:if test="${sessionScope.user.hasAnyPermission('STOCKTAKE_VIEW', 'STOCKTAKE_MANAGE')}">
                <a href="${cp}/manage/warehouse/stocktake">Kiểm kê</a>
            </c:if>
            <c:if test="${sessionScope.user.hasAnyPermission('VARIANTS_VIEW', 'VARIANTS_MANAGE')}">
                <a href="${cp}/manage/warehouse/variants">Biến thể</a>
            </c:if>
            <c:if test="${sessionScope.user.hasPermission('SUPPLIERS_VIEW')}">
                <a href="${cp}/manage/warehouse/suppliers">Nhà cung cấp</a>
            </c:if>
        </c:if>

        <c:if test="${sessionScope.user.hasAnyPermission('PRODUCTS_VIEW', 'PRODUCTS_MANAGE', 'CATEGORIES_VIEW', 'CATEGORIES_MANAGE', 'BRANDS_VIEW', 'BRANDS_MANAGE', 'VOUCHERS_VIEW', 'VOUCHERS_MANAGE')}">
            <div style="margin-top: 15px; padding: 8px 12px 4px 12px; font-size: 11px; font-weight: 700; color: #888; text-transform: uppercase; letter-spacing: 0.5px;">
                SẢN PHẨM
            </div>
            <c:if test="${sessionScope.user.hasAnyPermission('PRODUCTS_VIEW', 'PRODUCTS_MANAGE')}">
                <a href="${cp}/manage/admin/products">Sản phẩm</a>
            </c:if>
            <c:if test="${sessionScope.user.hasAnyPermission('CATEGORIES_VIEW', 'CATEGORIES_MANAGE')}">
                <a href="${cp}/manage/admin/categories">Danh mục</a>
            </c:if>
            <c:if test="${sessionScope.user.hasAnyPermission('BRANDS_VIEW', 'BRANDS_MANAGE')}">
                <a href="${cp}/manage/admin/brands">Thương hiệu</a>
            </c:if>
            <c:if test="${sessionScope.user.hasAnyPermission('VOUCHERS_VIEW', 'VOUCHERS_MANAGE')}">
                <a href="${cp}/manage/admin/vouchers">Voucher</a>
            </c:if>
        </c:if>

        <c:if test="${sessionScope.user.hasAnyPermission('BANNERS_VIEW', 'BANNERS_MANAGE', 'POSTS_VIEW', 'POSTS_MANAGE', 'NOTIFICATIONS_VIEW', 'NOTIFICATIONS_MANAGE')}">
            <div style="margin-top: 15px; padding: 8px 12px 4px 12px; font-size: 11px; font-weight: 700; color: #888; text-transform: uppercase; letter-spacing: 0.5px;">
                MARKETING
            </div>
            <c:if test="${sessionScope.user.hasAnyPermission('BANNERS_VIEW', 'BANNERS_MANAGE')}">
                <a href="${cp}/manage/admin/banners">Banner</a>
            </c:if>
            <c:if test="${sessionScope.user.hasAnyPermission('POSTS_VIEW', 'POSTS_MANAGE')}">
                <a href="${cp}/manage/admin/posts">Bài viết</a>
            </c:if>
            <c:if test="${sessionScope.user.hasAnyPermission('NOTIFICATIONS_VIEW', 'NOTIFICATIONS_MANAGE')}">
                <a href="${cp}/manage/admin/notifications">Thông báo</a>
            </c:if>
        </c:if>

        <c:if test="${sessionScope.user.hasAnyPermission('STATISTICS_VIEW', 'REPORTS_VIEW')}">
            <div style="margin-top: 15px; padding: 8px 12px 4px 12px; font-size: 11px; font-weight: 700; color: #888; text-transform: uppercase; letter-spacing: 0.5px;">
                BÁO CÁO
            </div>
            <c:if test="${sessionScope.user.hasPermission('STATISTICS_VIEW')}">
                <a href="${cp}/manage/admin/statistics">Thống kê</a>
            </c:if>
            <c:if test="${sessionScope.user.hasPermission('REPORTS_VIEW')}">
                <a href="${cp}/manage/warehouse/reports">Báo cáo</a>
            </c:if>
        </c:if>

        <c:if test="${sessionScope.user.hasAnyPermission('ACCOUNTS_VIEW', 'ACCOUNTS_MANAGE', 'ROLES_VIEW', 'ROLES_MANAGE', 'PERMISSIONS_VIEW', 'PERMISSIONS_MANAGE')}">
            <div style="margin-top: 15px; padding: 8px 12px 4px 12px; font-size: 11px; font-weight: 700; color: #888; text-transform: uppercase; letter-spacing: 0.5px;">
                HỆ THỐNG
            </div>
            <c:if test="${sessionScope.user.hasAnyPermission('ACCOUNTS_VIEW', 'ACCOUNTS_MANAGE')}">
                <a href="${cp}/manage/admin/accounts">Tài khoản</a>
            </c:if>
            <c:if test="${sessionScope.user.hasAnyPermission('ROLES_VIEW', 'ROLES_MANAGE')}">
                <a href="${cp}/manage/admin/roles">Vai trò</a>
            </c:if>
            <c:if test="${sessionScope.user.hasAnyPermission('PERMISSIONS_VIEW', 'PERMISSIONS_MANAGE')}">
                <a href="${cp}/manage/admin/permissions">Phân quyền</a>
            </c:if>
        </c:if>

        <c:if test="${sessionScope.user.hasAnyPermission('PROFILE_VIEW', 'PROFILE_MANAGE')}">
            <div style="margin-top: 15px; padding: 8px 12px 4px 12px; font-size: 11px; font-weight: 700; color: #888; text-transform: uppercase; letter-spacing: 0.5px;">
                TÀI KHOẢN
            </div>
            <c:if test="${sessionScope.user.hasAnyPermission('PROFILE_VIEW', 'PROFILE_MANAGE')}">
                <a href="${cp}/page/profile">Thông tin cá nhân</a>
            </c:if>
            <c:if test="${sessionScope.user.hasPermission('PROFILE_MANAGE')}">
                <a href="${cp}/page/change-password">Đổi mật khẩu</a>
            </c:if>
        </c:if>

    </nav>

    <a class="logout-link" href="${cp}/auth/logout">
        Đăng xuất
    </a>

</aside>