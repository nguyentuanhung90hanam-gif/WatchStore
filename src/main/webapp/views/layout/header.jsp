<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="cp" value="${pageContext.request.contextPath}" scope="request" />
<c:set var="assetVersion" value="20260803.3" scope="request" />
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${pageTitle} | WatchStore</title>
    <meta name="description" content="WatchStore - đồng hồ nam chính hãng, tuyển chọn cho phong cách hiện đại">
    <meta name="theme-color" content="#11110f">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans:wght@400;500;600;700;800&amp;family=Noto+Serif:wght@400;500;600;700&amp;display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${cp}/assets/css/app.css?v=${assetVersion}">
</head>
<body class="${adminLayout ? 'admin-body' : 'storefront-body'}">
<c:if test="${not adminLayout}">
    <div class="announcement pro-promo" data-promo>
        <div class="page-shell pro-promo-inner">
            <span><b>ƯU ĐÃI ĐẶC QUYỀN</b> · Miễn phí vận chuyển toàn quốc cho đơn từ 1.000.000₫</span>
            <a href="${cp}/page/vouchers">Khám phá ưu đãi <span aria-hidden="true">→</span></a>
            <button type="button" data-promo-close aria-label="Đóng thông báo">×</button>
        </div>
    </div>
    <div class="pro-utility">
        <div class="page-shell pro-utility-inner">
            <div><a href="tel:19006868">Hotline: <b>1900 6868</b></a><span>Hỗ trợ 08:00–21:00</span></div>
            <div><a href="#showroom">Hệ thống showroom</a><a href="${cp}/page/news">Tạp chí đồng hồ</a><a href="#services">Bảo hành chính hãng</a></div>
        </div>
    </div>
    <header class="site-header pro-header" data-site-header>
        <div class="header-inner pro-header-inner">
            <button class="icon-button menu-button" data-drawer-open aria-label="Mở menu">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M4 12h16M4 17h16"/></svg>
            </button>
            <form class="header-search pro-header-search" action="${cp}/page/products" method="get">
                <label for="global-search">Tìm kiếm</label>
                <input id="global-search" name="q" placeholder="Mẫu đồng hồ, thương hiệu..." autocomplete="off">
                <button aria-label="Tìm kiếm">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></svg>
                </button>
            </form>
            <a class="logo pro-logo" href="${cp}/page/home" aria-label="WatchStore - Trang chủ">
                <span>W</span><span class="pro-logo-copy"><b>WATCHSTORE</b><small>THE MEN'S WATCH ATELIER</small></span>
            </a>
            <div class="header-actions pro-header-actions">
                <a class="pro-action hide-tablet" href="#showroom" aria-label="Tìm showroom">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z"/><circle cx="12" cy="10" r="2.5"/></svg><span>Showroom</span>
                </a>
                <c:choose>
                    <c:when test="${empty sessionScope.user}">
                        <a class="pro-action hide-mobile" href="${cp}/auth/login" aria-label="Đăng nhập">
                            <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4.5 21a7.5 7.5 0 0 1 15 0"/></svg><span>Đăng nhập</span>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${sessionScope.user.role == 'ADMIN' || sessionScope.user.role == 'EMPLOYEE'}">
                            <a class="pro-action hide-mobile" href="${sessionScope.user.role == 'ADMIN' ? cp.concat('/manage/admin/dashboard') : cp.concat('/manage/sales/dashboard')}" style="color:#d4af37; font-weight:700; display:inline-flex; align-items:center; gap:6px;" aria-label="Trang quản trị">
                                <svg viewBox="0 0 24 24" aria-hidden="true" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                                <span>Trang quản trị</span>
                            </a>
                        </c:if>
                        <div class="user-avatar-dropdown" style="position:relative; display:inline-block;">
                            <a class="pro-action hide-mobile" href="${cp}/page/profile" aria-label="Tài khoản">
                                <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4.5 21a7.5 7.5 0 0 1 15 0"/></svg><span>${sessionScope.user.fullName}</span>
                            </a>
                            <div class="dropdown-menu-box" style="display:none; position:absolute; right:0; top:100%; background:#fff; min-width:180px; box-shadow:0 4px 16px rgba(0,0,0,0.15); border-radius:8px; padding:8px 0; z-index:1000; border:1px solid #eee;">
                                <c:if test="${sessionScope.user.role == 'ADMIN' || sessionScope.user.role == 'EMPLOYEE'}">
                                    <a href="${sessionScope.user.role == 'ADMIN' ? cp.concat('/manage/admin/dashboard') : cp.concat('/manage/sales/dashboard')}" style="display:block; padding:8px 16px; color:#d4af37; font-weight:700; font-size:13px; text-decoration:none; border-bottom:1px solid #eee;">⚙ Trang quản trị</a>
                                </c:if>
                                <a href="${cp}/page/profile" style="display:block; padding:8px 16px; color:#333; font-size:13px; text-decoration:none;">Thông tin tài khoản</a>
                                <a href="${cp}/page/profile" style="display:block; padding:8px 16px; color:#333; font-size:13px; text-decoration:none;">Chỉnh sửa hồ sơ</a>
                                <a href="${cp}/page/change-password" style="display:block; padding:8px 16px; color:#333; font-size:13px; text-decoration:none;">Đổi mật khẩu</a>
                                <a href="${cp}/auth/logout" style="display:block; padding:8px 16px; color:#e53935; font-size:13px; text-decoration:none; border-top:1px solid #eee;">Đăng xuất</a>
                            </div>
                        </div>
                        <style>
                            .user-avatar-dropdown:hover .dropdown-menu-box,
                            .user-avatar-dropdown:focus-within .dropdown-menu-box { display: block !important; }
                            .user-avatar-dropdown .dropdown-menu-box a:hover { background: #f5f5f5; color: #d4af37 !important; }
                        </style>
                    </c:otherwise>
                </c:choose>
                <a class="pro-action icon-only hide-mobile" href="${cp}/page/wishlist" aria-label="Yêu thích">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.8-7.5 1.1-1.1a5.5 5.5 0 0 0-.1-7.8Z"/></svg>
                </a>
                <a class="pro-action pro-cart" href="${cp}/cart/view" aria-label="Giỏ hàng">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 8h12l1 13H5L6 8Z"/><path d="M9 9V6a3 3 0 0 1 6 0v3"/></svg><span class="hide-mobile">Giỏ hàng</span><b class="badge-count gold">${empty cartCount ? 0 : cartCount}</b>
                </a>
            </div>
        </div>
    </header>
</c:if>
<c:if test="${not empty sessionScope.flash}">
    <div class="toast show">✓ ${sessionScope.flash}</div>
    <c:remove var="flash" scope="session" />
</c:if>
