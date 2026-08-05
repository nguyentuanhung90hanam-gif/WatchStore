<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="cp" value="${pageContext.request.contextPath}" scope="request" />
<c:set var="assetVersion" value="20260805.1" scope="request" />
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
    <c:if test="${adminLayout}">
        <link rel="stylesheet" href="${cp}/assets/css/admin.css?v=${assetVersion}">
    </c:if>
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
                        <a class="pro-action hide-mobile" href="${cp}/page/profile" aria-label="Tài khoản">
                            <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4.5 21a7.5 7.5 0 0 1 15 0"/></svg><span>Tài khoản</span>
                        </a>
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
    <div class="toast show" role="status">✓ ${sessionScope.flash}</div>
    <c:remove var="flash" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.flashError}">
    <div class="toast toast-error show" role="alert">! ${sessionScope.flashError}</div>
    <c:remove var="flashError" scope="session" />
</c:if>
