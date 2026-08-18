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
            <%-- MODE 1: ADMIN (SUPER ADMIN - FULL ACCESS) --%>
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

                <div class="sidebar-section-title">REVIEW &amp; COMMENT</div>
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

            <%-- MODE 2: EMPLOYEE (DYNAMIC PHÂN QUYỀN NGHIỆP VỤ) --%>
            <c:when test="${sessionScope.user.role == 'EMPLOYEE'}">
                <div class="sidebar-section-title">TỔNG QUAN</div>
                <a href="${cp}/manage/sales/dashboard" class="${fn:contains(uri,'/manage/sales/dashboard') ? 'active' : ''}">Dashboard</a>

                <%-- NHÓM 1: SẢN PHẨM --%>
                <c:if test="${sessionScope.userPermissions.contains('PRODUCT_VIEW') || sessionScope.userPermissions.contains('PRODUCT_CREATE') || sessionScope.userPermissions.contains('PRODUCT_EDIT')}">
                    <div class="sidebar-section-title">SẢN PHẨM</div>
                    <a href="${cp}/manage/admin/products" class="${fn:contains(uri,'/manage/admin/product') ? 'active' : ''}">Sản phẩm</a>
                    <a href="${cp}/manage/admin/categories" class="${fn:contains(uri,'/manage/admin/categor') ? 'active' : ''}">Danh mục</a>
                    <a href="${cp}/manage/admin/brands" class="${fn:contains(uri,'/manage/admin/brand') ? 'active' : ''}">Thương hiệu</a>
                </c:if>

                <%-- NHÓM 2: VOUCHER --%>
                <c:if test="${sessionScope.userPermissions.contains('VOUCHER_VIEW') || sessionScope.userPermissions.contains('VOUCHER_CREATE') || sessionScope.userPermissions.contains('VOUCHER_EDIT')}">
                    <div class="sidebar-section-title">VOUCHER</div>
                    <a href="${cp}/manage/admin/vouchers" class="${fn:contains(uri,'/manage/admin/voucher') ? 'active' : ''}">Voucher</a>
                </c:if>

                <%-- NHÓM 3: ĐƠN HÀNG --%>
                <c:if test="${sessionScope.userPermissions.contains('ORDER_VIEW') || sessionScope.userPermissions.contains('SALES_ORDER') || sessionScope.userPermissions.contains('ORDER_CREATE') || sessionScope.userPermissions.contains('ORDER_EDIT') || sessionScope.userPermissions.contains('ORDER_APPROVE') || sessionScope.userPermissions.contains('ORDER_EXPORT')}">
                    <div class="sidebar-section-title">ĐƠN HÀNG</div>
                    <a href="${cp}/manage/sales/orders" class="${fn:contains(uri,'/manage/sales/order') ? 'active' : ''}">Quản lý đơn hàng</a>
                </c:if>

                <%-- NHÓM 4: KHÁCH HÀNG --%>
                <c:if test="${sessionScope.userPermissions.contains('CUSTOMER_VIEW') || sessionScope.userPermissions.contains('SALES_CUSTOMER') || sessionScope.userPermissions.contains('CUSTOMER_CREATE') || sessionScope.userPermissions.contains('CUSTOMER_EDIT')}">
                    <div class="sidebar-section-title">KHÁCH HÀNG</div>
                    <a href="${cp}/manage/sales/customers" class="${fn:contains(uri,'/manage/sales/customer') ? 'active' : ''}">Danh sách khách hàng</a>
                </c:if>

                <%-- NHÓM 5: REVIEW & COMMENT --%>
                <c:if test="${sessionScope.userPermissions.contains('CUSTOMER_VIEW') || sessionScope.userPermissions.contains('SALES_CUSTOMER') || sessionScope.userPermissions.contains('CUSTOMER_EDIT')}">
                    <div class="sidebar-section-title">REVIEW &amp; COMMENT</div>
                    <a href="${cp}/manage/sales/reviews" class="${fn:contains(uri,'/manage/sales/review') ? 'active' : ''}">Kiểm duyệt đánh giá</a>
                    <a href="${cp}/manage/sales/comments" class="${fn:contains(uri,'/manage/sales/comment') ? 'active' : ''}">Bình luận</a>
                </c:if>

                <%-- NHÓM 6: BẢO HÀNH --%>
                <c:if test="${sessionScope.userPermissions.contains('SALES_WARRANTY')}">
                    <div class="sidebar-section-title">BẢO HÀNH</div>
                    <a href="${cp}/manage/sales/warranty" class="${fn:contains(uri,'/manage/sales/warranty') ? 'active' : ''}">Quản lý bảo hành</a>
                </c:if>

                <%-- NHÓM 7: BÁO CÁO --%>
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