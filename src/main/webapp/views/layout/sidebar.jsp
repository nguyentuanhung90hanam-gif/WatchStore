<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<aside class="portal-sidebar" data-sidebar>
    <a class="portal-logo" href="${cp}/page/home"><span>W</span><b>ATCHSTORE</b></a>
    <div class="portal-user">
        <div>${empty sessionScope.user ? 'TN' : fn:substring(sessionScope.user.fullName,0,1)}</div>
        <span><b>${empty sessionScope.user ? 'Quản trị viên' : sessionScope.user.fullName}</b>
            <small>${empty sessionScope.user ? 'ADMIN' : sessionScope.user.role.label}</small>
        </span>
    </div>
    <c:set var="uri" value="${pageContext.request.requestURI}" />
    <nav>
        <c:choose>
            <c:when test="${sessionScope.user.role == 'ADMIN'}">
                <div class="sidebar-section-title">TỔNG QUAN</div>
                <a href="${cp}/manage/admin/dashboard" class="${fn:contains(uri,'/manage/admin/dashboard') ? 'active' : ''}">Dashboard</a>
                <div class="sidebar-section-title">HỆ THỐNG</div>
                <a href="${cp}/manage/admin/accounts" class="${fn:contains(uri,'/manage/admin/account') ? 'active' : ''}">Tài khoản</a>
                <a href="${cp}/manage/admin/roles" class="${fn:contains(uri,'/manage/admin/role') ? 'active' : ''}">Vai trò</a>
                <a href="${cp}/manage/admin/permissions" class="${fn:contains(uri,'/manage/admin/permission') ? 'active' : ''}">Phân quyền</a>
                <div class="sidebar-section-title">SẢN PHẨM</div>
                <a href="${cp}/manage/admin/products" class="${fn:contains(uri,'/manage/admin/product') ? 'active' : ''}">Sản phẩm</a>
                <a href="${cp}/manage/admin/categories" class="${fn:contains(uri,'/manage/admin/categor') ? 'active' : ''}">Danh mục</a>
                <a href="${cp}/manage/admin/brands" class="${fn:contains(uri,'/manage/admin/brand') ? 'active' : ''}">Thương hiệu</a>
                <div class="sidebar-section-title">BÁN HÀNG</div>
                <a href="${cp}/manage/sales/orders" class="${fn:contains(uri,'/manage/sales/order') ? 'active' : ''}">Đơn hàng</a>
                <a href="${cp}/manage/sales/customers" class="${fn:contains(uri,'/manage/sales/customer') ? 'active' : ''}">Khách hàng</a>
                <a href="${cp}/manage/sales/reviews" class="${fn:contains(uri,'/manage/sales/review') ? 'active' : ''}">Review</a>
                <a href="${cp}/manage/sales/comments" class="${fn:contains(uri,'/manage/sales/comment') ? 'active' : ''}">Comment</a>
                <div class="sidebar-section-title">BẢO HÀNH</div>
                <a href="${cp}/manage/sales/warranty" class="${fn:contains(uri,'/manage/sales/warranty') ? 'active' : ''}">Quản lý bảo hành</a>
                <div class="sidebar-section-title">MARKETING &amp; NỘI DUNG</div>
                <a href="${cp}/manage/admin/vouchers" class="${fn:contains(uri,'/manage/admin/voucher') ? 'active' : ''}">Voucher</a>
                <a href="${cp}/manage/admin/banners" class="${fn:contains(uri,'/manage/admin/banner') ? 'active' : ''}">Banner</a>
                <a href="${cp}/manage/admin/posts" class="${fn:contains(uri,'/manage/admin/post') ? 'active' : ''}">Bài viết</a>
                <div class="sidebar-section-title">BÁO CÁO</div>
                <a href="${cp}/manage/admin/statistics" class="${fn:contains(uri,'/manage/admin/statistics') || fn:contains(uri,'/manage/admin/reports') || fn:contains(uri,'/manage/sales/report') ? 'active' : ''}">Thống kê &amp; Báo cáo</a>
            </c:when>
            <c:when test="${sessionScope.user.role == 'EMPLOYEE'}">
                <div class="sidebar-section-title">TỔNG QUAN</div>
                <a href="${cp}/manage/sales/dashboard" class="${fn:contains(uri,'/manage/sales/dashboard') ? 'active' : ''}">Dashboard</a>

                <c:if test="${sessionScope.userPermissions.contains('PRODUCT_VIEW')}">
                    <div class="sidebar-section-title">SẢN PHẨM</div>
                    <a href="${cp}/manage/admin/products" class="${fn:contains(uri,'/manage/admin/product') ? 'active' : ''}">Sản phẩm</a>
                    <a href="${cp}/manage/admin/categories" class="${fn:contains(uri,'/manage/admin/categor') ? 'active' : ''}">Danh mục</a>
                    <a href="${cp}/manage/admin/brands" class="${fn:contains(uri,'/manage/admin/brand') ? 'active' : ''}">Thương hiệu</a>
                </c:if>

                <c:if test="${sessionScope.userPermissions.contains('ORDER_VIEW') || sessionScope.userPermissions.contains('SALES_ORDER') || sessionScope.userPermissions.contains('CUSTOMER_VIEW') || sessionScope.userPermissions.contains('SALES_CUSTOMER') || sessionScope.userPermissions.contains('PRODUCT_VIEW')}">
                    <div class="sidebar-section-title">BÁN HÀNG</div>
                    <c:if test="${sessionScope.userPermissions.contains('ORDER_VIEW') || sessionScope.userPermissions.contains('SALES_ORDER')}">
                        <a href="${cp}/manage/sales/orders" class="${fn:contains(uri,'/manage/sales/order') ? 'active' : ''}">Đơn hàng</a>
                    </c:if>
                    <c:if test="${sessionScope.userPermissions.contains('CUSTOMER_VIEW') || sessionScope.userPermissions.contains('SALES_CUSTOMER')}">
                        <a href="${cp}/manage/sales/customers" class="${fn:contains(uri,'/manage/sales/customer') ? 'active' : ''}">Khách hàng</a>
                    </c:if>
                    <c:if test="${sessionScope.userPermissions.contains('PRODUCT_VIEW') || sessionScope.userPermissions.contains('ORDER_VIEW')}">
                        <a href="${cp}/manage/sales/reviews" class="${fn:contains(uri,'/manage/sales/review') ? 'active' : ''}">Review</a>
                        <a href="${cp}/manage/sales/comments" class="${fn:contains(uri,'/manage/sales/comment') ? 'active' : ''}">Comment</a>
                    </c:if>
                </c:if>

                <c:if test="${sessionScope.userPermissions.contains('SALES_WARRANTY') || sessionScope.userPermissions.contains('WARRANTY_VIEW')}">
                    <div class="sidebar-section-title">BẢO HÀNH</div>
                    <a href="${cp}/manage/sales/warranty" class="${fn:contains(uri,'/manage/sales/warranty') ? 'active' : ''}">Quản lý bảo hành</a>
                </c:if>

                <c:if test="${sessionScope.userPermissions.contains('VOUCHER_VIEW')}">
                    <div class="sidebar-section-title">MARKETING &amp; NỘI DUNG</div>
                    <a href="${cp}/manage/admin/vouchers" class="${fn:contains(uri,'/manage/admin/voucher') ? 'active' : ''}">Voucher</a>
                    <a href="${cp}/manage/admin/banners" class="${fn:contains(uri,'/manage/admin/banner') ? 'active' : ''}">Banner</a>
                    <a href="${cp}/manage/admin/posts" class="${fn:contains(uri,'/manage/admin/post') ? 'active' : ''}">Bài viết</a>
                </c:if>

                <c:if test="${sessionScope.userPermissions.contains('REPORT_VIEW') || sessionScope.userPermissions.contains('SALES_REPORT') || sessionScope.userPermissions.contains('REPORT_EXPORT')}">
                    <div class="sidebar-section-title">BÁO CÁO</div>
                    <a href="${cp}/manage/admin/statistics" class="${fn:contains(uri,'/manage/admin/statistics') || fn:contains(uri,'/manage/admin/reports') || fn:contains(uri,'/manage/sales/report') ? 'active' : ''}">Thống kê &amp; Báo cáo</a>
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