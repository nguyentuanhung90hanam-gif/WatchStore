<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set
    var="uri"
    value="${pageContext.request.requestURI}"
/>

<aside
    class="portal-sidebar"
    data-sidebar
>

    <style>
        .portal-sidebar {
            font-family:
                Arial,
                "Segoe UI",
                sans-serif;
        }

        .portal-sidebar nav {
            padding: 12px 8px;
        }

        .sidebar-link,
        .sidebar-group > summary {
            min-height: 38px;
            padding: 0 10px;

            display: flex;
            align-items: center;

            border-radius: 5px;

            color: #aaa79f;

            font-size: 11px;
            font-weight: 600;

            text-decoration: none;

            cursor: pointer;
        }

        .sidebar-link:hover,
        .sidebar-group > summary:hover {
            background: #1c1c19;
            color: #fff;
        }

        .sidebar-link.active {
            background: #24231f;
            color: #fff;
        }

        .sidebar-group {
            margin: 2px 0;
        }

        .sidebar-group > summary {
            list-style: none;
            color: #f0eee8;
            font-weight: 700;
        }

        .sidebar-group > summary::-webkit-details-marker {
            display: none;
        }

        .sidebar-group > summary::after {
            content: "›";
            margin-left: auto;
            font-size: 16px;
            color: #77736b;
        }

        .sidebar-group[open] > summary::after {
            transform: rotate(90deg);
            color: #d5a940;
        }

        .sidebar-submenu {
            margin-left: 16px;
            padding: 3px 0 5px 12px;

            border-left: 1px solid #302f2b;
        }

        .sidebar-submenu .sidebar-link {
            min-height: 34px;
            font-size: 11px;
            font-weight: 500;
        }

        .sidebar-title {
            margin: 14px 8px 5px;

            color: #77736b;

            font-size: 9px;
            font-weight: 700;

            letter-spacing: .08em;
        }

        .sidebar-icon {
            width: 18px;
            margin-right: 7px;

            text-align: center;
            color: #8d8980;
        }

        .sidebar-group[open]
        .sidebar-icon {
            color: #d5a940;
        }

        .sidebar-user-name {
            font-size: 11px;
        }

        .sidebar-user-role {
            font-size: 9px;
        }

        .logout-link {
            font-size: 10px !important;
        }
    </style>

    <!-- LOGO -->

    <a
        class="portal-logo"
        href="${cp}/page/home"
    >
        <span>W</span>
        <b>ATCHSTORE</b>
    </a>

    <!-- USER -->

    <div class="portal-user">

        <div>
            ${empty sessionScope.user
                ? 'TN'
                : fn:substring(
                    sessionScope.user.fullName,
                    0,
                    1
                )}
        </div>

        <span>

            <b class="sidebar-user-name">
                ${empty sessionScope.user
                    ? 'Quản trị viên'
                    : sessionScope.user.fullName}
            </b>

            <small class="sidebar-user-role">
                ${empty sessionScope.user
                    ? 'ADMIN'
                    : sessionScope.user.role.label}
            </small>

        </span>

    </div>

    <nav>

        <!-- =========================
             ADMIN
             ========================= -->

        <c:if test="${sessionScope.user.role == 'ADMIN'}">

            <div class="sidebar-title">
                TỔNG QUAN
            </div>

            <a
                href="${cp}/manage/admin/dashboard"
                class="sidebar-link
                    ${fn:contains(
                        uri,
                        '/manage/admin/dashboard'
                    ) ? 'active' : ''}"
            >
                <span class="sidebar-icon"></span>
                Dashboard
            </a>

            <!-- HỆ THỐNG -->

            <details
                class="sidebar-group"
                ${fn:contains(uri, '/manage/admin/account')
                    || fn:contains(uri, '/manage/admin/role')
                    || fn:contains(uri, '/manage/admin/permission')
                    ? 'open' : ''}
            >

                <summary>
                    <span class="sidebar-icon"></span>
                    HỆ THỐNG
                </summary>

                <div class="sidebar-submenu">

                    <a
                        href="${cp}/manage/admin/accounts"
                        class="sidebar-link
                            ${fn:contains(
                                uri,
                                '/manage/admin/account'
                            ) ? 'active' : ''}"
                    >
                        Tài khoản
                    </a>

                    <a
                        href="${cp}/manage/admin/roles"
                        class="sidebar-link
                            ${fn:contains(
                                uri,
                                '/manage/admin/role'
                            ) ? 'active' : ''}"
                    >
                        Vai trò
                    </a>

                    <a
                        href="${cp}/manage/admin/permissions"
                        class="sidebar-link
                            ${fn:contains(
                                uri,
                                '/manage/admin/permission'
                            ) ? 'active' : ''}"
                    >
                        Phân quyền
                    </a>

                </div>

            </details>

            <!-- SẢN PHẨM -->

            <details
                class="sidebar-group"
                ${fn:contains(uri, '/manage/admin/product')
                    || fn:contains(uri, '/manage/admin/categor')
                    || fn:contains(uri, '/manage/admin/brand')
                    ? 'open' : ''}
            >

                <summary>
                    <span class="sidebar-icon"></span>
                    SẢN PHẨM
                </summary>

                <div class="sidebar-submenu">

                    <a
                        href="${cp}/manage/admin/products"
                        class="sidebar-link
                            ${fn:contains(
                                uri,
                                '/manage/admin/product'
                            ) ? 'active' : ''}"
                    >
                        Sản phẩm
                    </a>

                    <a
                        href="${cp}/manage/admin/categories"
                        class="sidebar-link
                            ${fn:contains(
                                uri,
                                '/manage/admin/categor'
                            ) ? 'active' : ''}"
                    >
                        Danh mục
                    </a>

                    <a
                        href="${cp}/manage/admin/brands"
                        class="sidebar-link
                            ${fn:contains(
                                uri,
                                '/manage/admin/brand'
                            ) ? 'active' : ''}"
                    >
                        Thương hiệu
                    </a>

                </div>

            </details>

            <!-- BÁN HÀNG -->

            <details
                class="sidebar-group"
                ${fn:contains(uri, '/manage/sales/order')
                    || fn:contains(uri, '/manage/sales/customer')
                    ? 'open' : ''}
            >

                <summary>
                    <span class="sidebar-icon"></span>
                    BÁN HÀNG
                </summary>

                <div class="sidebar-submenu">

                    <a
                        href="${cp}/manage/sales/orders"
                        class="sidebar-link
                            ${fn:contains(
                                uri,
                                '/manage/sales/order'
                            ) ? 'active' : ''}"
                    >
                        Đơn hàng
                    </a>

                    <a
                        href="${cp}/manage/sales/customers"
                        class="sidebar-link
                            ${fn:contains(
                                uri,
                                '/manage/sales/customer'
                            ) ? 'active' : ''}"
                    >
                        Khách hàng
                    </a>

                </div>

            </details>

            <!-- REVIEW -->

            <details
                class="sidebar-group"
                ${fn:contains(uri, '/manage/sales/review')
                    || fn:contains(uri, '/manage/sales/comment')
                    ? 'open' : ''}
            >

                <summary>
                    <span class="sidebar-icon"></span>
                    REVIEW &amp; COMMENT
                </summary>

                <div class="sidebar-submenu">

                    <a
                        href="${cp}/manage/sales/reviews"
                        class="sidebar-link
                            ${fn:contains(
                                uri,
                                '/manage/sales/review'
                            ) ? 'active' : ''}"
                    >
                        Review
                    </a>

                    <a
                        href="${cp}/manage/sales/comments"
                        class="sidebar-link
                            ${fn:contains(
                                uri,
                                '/manage/sales/comment'
                            ) ? 'active' : ''}"
                    >
                        Comment
                    </a>

                </div>

            </details>

            <!-- BẢO HÀNH -->

            <details
                class="sidebar-group"
                ${fn:contains(
                    uri,
                    '/manage/sales/warranty'
                ) ? 'open' : ''}
            >

                <summary>
                    <span class="sidebar-icon"></span>
                    BẢO HÀNH
                </summary>

                <div class="sidebar-submenu">

                    <a
                        href="${cp}/manage/sales/warranty"
                        class="sidebar-link
                            ${fn:contains(
                                uri,
                                '/manage/sales/warranty'
                            ) ? 'active' : ''}"
                    >
                        Quản lý bảo hành
                    </a>

                </div>

            </details>

            <!-- KHUYẾN MÃI -->

            <details
                class="sidebar-group"
                ${fn:contains(
                    uri,
                    '/manage/admin/voucher'
                ) ? 'open' : ''}
            >

                <summary>
                    <span class="sidebar-icon"></span>
                    KHUYẾN MÃI
                </summary>

                <div class="sidebar-submenu">

                    <a
                        href="${cp}/manage/admin/vouchers"
                        class="sidebar-link
                            ${fn:contains(
                                uri,
                                '/manage/admin/voucher'
                            ) ? 'active' : ''}"
                    >
                        Voucher
                    </a>

                </div>

            </details>

            <!-- BÁO CÁO -->

            <details
                class="sidebar-group"
                ${fn:contains(
                    uri,
                    '/manage/admin/statistics'
                ) || fn:contains(
                    uri,
                    '/manage/admin/reports'
                ) || fn:contains(
                    uri,
                    '/manage/sales/report'
                ) ? 'open' : ''}
            >

                <summary>
                    <span class="sidebar-icon"></span>
                    BÁO CÁO
                </summary>

                <div class="sidebar-submenu">

                    <a
                        href="${cp}/manage/admin/statistics"
                        class="sidebar-link
                            ${fn:contains(
                                uri,
                                '/manage/admin/statistics'
                            ) ? 'active' : ''}"
                    >
                        Thống kê &amp; Báo cáo
                    </a>

                </div>

            </details>

        </c:if>

        <!-- =========================
             EMPLOYEE
             ========================= -->

        <c:if test="${sessionScope.user.role == 'EMPLOYEE'}">

            <div class="sidebar-title">
                TỔNG QUAN
            </div>

            <a
                href="${cp}/manage/sales/dashboard"
                class="sidebar-link
                    ${fn:contains(
                        uri,
                        '/manage/sales/dashboard'
                    ) ? 'active' : ''}"
            >
                <span class="sidebar-icon">⌂</span>
                Dashboard
            </a>

            <!-- SẢN PHẨM -->

            <c:if test="${sessionScope.userPermissions.contains(
                'PRODUCT_VIEW'
            ) || sessionScope.userPermissions.contains(
                'PRODUCT_CREATE'
            ) || sessionScope.userPermissions.contains(
                'PRODUCT_EDIT'
            )}">

                <details
                    class="sidebar-group"
                    ${fn:contains(uri, '/manage/admin/product')
                        || fn:contains(uri, '/manage/admin/categor')
                        || fn:contains(uri, '/manage/admin/brand')
                        ? 'open' : ''}
                >

                    <summary>
                        <span class="sidebar-icon"></span>
                        SẢN PHẨM
                    </summary>

                    <div class="sidebar-submenu">

                        <a
                            href="${cp}/manage/admin/products"
                            class="sidebar-link
                                ${fn:contains(
                                    uri,
                                    '/manage/admin/product'
                                ) ? 'active' : ''}"
                        >
                            Sản phẩm
                        </a>

                        <a
                            href="${cp}/manage/admin/categories"
                            class="sidebar-link
                                ${fn:contains(
                                    uri,
                                    '/manage/admin/categor'
                                ) ? 'active' : ''}"
                        >
                            Danh mục
                        </a>

                        <a
                            href="${cp}/manage/admin/brands"
                            class="sidebar-link
                                ${fn:contains(
                                    uri,
                                    '/manage/admin/brand'
                                ) ? 'active' : ''}"
                        >
                            Thương hiệu
                        </a>

                    </div>

                </details>

            </c:if>

            <!-- VOUCHER -->

            <c:if test="${sessionScope.userPermissions.contains(
                'VOUCHER_VIEW'
            ) || sessionScope.userPermissions.contains(
                'VOUCHER_CREATE'
            ) || sessionScope.userPermissions.contains(
                'VOUCHER_EDIT'
            )}">

                <details
                    class="sidebar-group"
                    ${fn:contains(
                        uri,
                        '/manage/admin/voucher'
                    ) ? 'open' : ''}
                >

                    <summary>
                        <span class="sidebar-icon">%</span>
                        VOUCHER
                    </summary>

                    <div class="sidebar-submenu">

                        <a
                            href="${cp}/manage/admin/vouchers"
                            class="sidebar-link
                                ${fn:contains(
                                    uri,
                                    '/manage/admin/voucher'
                                ) ? 'active' : ''}"
                        >
                            Voucher
                        </a>

                    </div>

                </details>

            </c:if>

            <!-- ĐƠN HÀNG -->

            <c:if test="${sessionScope.userPermissions.contains(
                'ORDER_VIEW'
            ) || sessionScope.userPermissions.contains(
                'SALES_ORDER'
            ) || sessionScope.userPermissions.contains(
                'ORDER_CREATE'
            ) || sessionScope.userPermissions.contains(
                'ORDER_EDIT'
            ) || sessionScope.userPermissions.contains(
                'ORDER_APPROVE'
            ) || sessionScope.userPermissions.contains(
                'ORDER_EXPORT'
            )}">

                <details
                    class="sidebar-group"
                    ${fn:contains(
                        uri,
                        '/manage/sales/order'
                    ) ? 'open' : ''}
                >

                    <summary>
                        <span class="sidebar-icon"></span>
                        ĐƠN HÀNG
                    </summary>

                    <div class="sidebar-submenu">

                        <a
                            href="${cp}/manage/sales/orders"
                            class="sidebar-link
                                ${fn:contains(
                                    uri,
                                    '/manage/sales/order'
                                ) ? 'active' : ''}"
                        >
                            Quản lý đơn hàng
                        </a>

                    </div>

                </details>

            </c:if>

            <!-- KHÁCH HÀNG -->

            <c:if test="${sessionScope.userPermissions.contains(
                'CUSTOMER_VIEW'
            ) || sessionScope.userPermissions.contains(
                'SALES_CUSTOMER'
            ) || sessionScope.userPermissions.contains(
                'CUSTOMER_CREATE'
            ) || sessionScope.userPermissions.contains(
                'CUSTOMER_EDIT'
            )}">

                <details
                    class="sidebar-group"
                    ${fn:contains(
                        uri,
                        '/manage/sales/customer'
                    ) ? 'open' : ''}
                >

                    <summary>
                        <span class="sidebar-icon">○</span>
                        KHÁCH HÀNG
                    </summary>

                    <div class="sidebar-submenu">

                        <a
                            href="${cp}/manage/sales/customers"
                            class="sidebar-link
                                ${fn:contains(
                                    uri,
                                    '/manage/sales/customer'
                                ) ? 'active' : ''}"
                        >
                            Danh sách khách hàng
                        </a>

                    </div>

                </details>

            </c:if>

            <!-- REVIEW -->

            <c:if test="${sessionScope.userPermissions.contains(
                'CUSTOMER_VIEW'
            ) || sessionScope.userPermissions.contains(
                'SALES_CUSTOMER'
            ) || sessionScope.userPermissions.contains(
                'CUSTOMER_EDIT'
            )}">

                <details
                    class="sidebar-group"
                    ${fn:contains(uri, '/manage/sales/review')
                        || fn:contains(uri, '/manage/sales/comment')
                        ? 'open' : ''}
                >

                    <summary>
                        <span class="sidebar-icon"></span>
                        REVIEW &amp; COMMENT
                    </summary>

                    <div class="sidebar-submenu">

                        <a
                            href="${cp}/manage/sales/reviews"
                            class="sidebar-link
                                ${fn:contains(
                                    uri,
                                    '/manage/sales/review'
                                ) ? 'active' : ''}"
                        >
                            Kiểm duyệt đánh giá
                        </a>

                        <a
                            href="${cp}/manage/sales/comments"
                            class="sidebar-link
                                ${fn:contains(
                                    uri,
                                    '/manage/sales/comment'
                                ) ? 'active' : ''}"
                        >
                            Bình luận
                        </a>

                    </div>

                </details>

            </c:if>

            <!-- BẢO HÀNH -->

            <c:if test="${sessionScope.userPermissions.contains(
                'SALES_WARRANTY'
            )}">

                <details
                    class="sidebar-group"
                    ${fn:contains(
                        uri,
                        '/manage/sales/warranty'
                    ) ? 'open' : ''}
                >

                    <summary>
                        <span class="sidebar-icon"></span>
                        BẢO HÀNH
                    </summary>

                    <div class="sidebar-submenu">

                        <a
                            href="${cp}/manage/sales/warranty"
                            class="sidebar-link
                                ${fn:contains(
                                    uri,
                                    '/manage/sales/warranty'
                                ) ? 'active' : ''}"
                        >
                            Quản lý bảo hành
                        </a>

                    </div>

                </details>

            </c:if>

            <!-- BÁO CÁO -->

            <c:if test="${sessionScope.userPermissions.contains(
                'REPORT_VIEW'
            ) || sessionScope.userPermissions.contains(
                'SALES_REPORT'
            ) || sessionScope.userPermissions.contains(
                'REPORT_EXPORT'
            )}">

                <details
                    class="sidebar-group"
                    ${fn:contains(
                        uri,
                        '/manage/admin/statistics'
                    ) || fn:contains(
                        uri,
                        '/manage/admin/reports'
                    ) || fn:contains(
                        uri,
                        '/manage/sales/report'
                    ) ? 'open' : ''}
                >

                    <summary>
                        <span class="sidebar-icon"></span>
                        BÁO CÁO
                    </summary>

                    <div class="sidebar-submenu">

                        <a
                            href="${cp}/manage/admin/statistics"
                            class="sidebar-link
                                ${fn:contains(
                                    uri,
                                    '/manage/admin/statistics'
                                ) ? 'active' : ''}"
                        >
                            Thống kê &amp; Báo cáo
                        </a>

                    </div>

                </details>

            </c:if>

        </c:if>

    </nav>

    <!-- LOGOUT -->

    <a
        class="logout-link"
        href="${cp}/auth/logout"
    >
        Đăng xuất
    </a>

</aside>