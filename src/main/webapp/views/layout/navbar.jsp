<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="main-nav pro-nav" aria-label="Điều hướng chính">
    <div class="page-shell nav-inner pro-nav-inner">
        <a href="${cp}/page/home">Trang chủ</a>
        <div class="pro-nav-group">
            <a href="${cp}/page/products">Đồng hồ nam <span aria-hidden="true">⌄</span></a>
            <div class="pro-mega-menu">
                <div><small>BỘ MÁY</small><a href="${cp}/page/products?type=automatic">Đồng hồ Automatic</a><a href="${cp}/page/products?type=quartz">Đồng hồ Quartz</a><a href="${cp}/page/products?type=smart">Đồng hồ thông minh</a></div>
                <div><small>PHONG CÁCH</small><a href="${cp}/page/products?type=dress">Dress Watch</a><a href="${cp}/page/products?type=sport">Sport Watch</a><a href="${cp}/page/products?type=luxury">Luxury Watch</a></div>
                <a class="pro-mega-feature" href="${cp}/page/products?type=automatic"><span>MEN'S ESSENTIALS</span><b>Chọn một dấu ấn<br>cho riêng bạn</b><em>Khám phá ngay →</em></a>
            </div>
        </div>
        <div class="pro-nav-group">
            <a href="${cp}/page/products?brand=all">Thương hiệu <span aria-hidden="true">⌄</span></a>
            <div class="pro-mega-menu pro-brand-menu">
                <div><small>THỤY SĨ</small><a href="${cp}/page/products?brand=tissot">Tissot</a><a href="${cp}/page/products?brand=longines">Longines</a><a href="${cp}/page/products?brand=frederique-constant">Frederique Constant</a></div>
                <div><small>NHẬT BẢN</small><a href="${cp}/page/products?brand=seiko">Seiko</a><a href="${cp}/page/products?brand=orient">Orient</a><a href="${cp}/page/products?brand=casio">Casio</a></div>
                <div><small>PHONG CÁCH</small><a href="${cp}/page/products?brand=citizen">Citizen</a><a href="${cp}/page/products?brand=fossil">Fossil</a><a href="${cp}/page/products">Xem tất cả thương hiệu</a></div>
            </div>
        </div>
        <a href="${cp}/page/products?sort=new">Bộ sưu tập mới</a>
        <a href="${cp}/page/vouchers" class="nav-sale">Ưu đãi</a>
        <a href="${cp}/page/news">Tạp chí</a>
        <c:if test="${not empty sessionScope.user}"><a href="${cp}/orders/list">Đơn hàng</a></c:if>
    </div>
</nav>
<div class="mobile-drawer" data-drawer>
    <button class="drawer-overlay" data-drawer-close aria-label="Đóng menu"></button>
    <div class="drawer-panel pro-drawer">
        <div class="drawer-heading"><a class="logo pro-logo" href="${cp}/page/home"><span>W</span><span class="pro-logo-copy"><b>WATCHSTORE</b><small>MEN'S WATCH ATELIER</small></span></a><button class="icon-button" data-drawer-close aria-label="Đóng menu">×</button></div>
        <form class="pro-drawer-search" action="${cp}/page/products"><input name="q" placeholder="Tìm kiếm đồng hồ..."><button aria-label="Tìm kiếm">⌕</button></form>
        <small class="pro-drawer-label">KHÁM PHÁ WATCHSTORE</small>
        <a href="${cp}/page/home">Trang chủ <span>›</span></a>
        <a href="${cp}/page/products">Đồng hồ nam <span>›</span></a>
        <a href="${cp}/page/products?sort=new">Bộ sưu tập mới <span>›</span></a>
        <a href="${cp}/page/vouchers">Ưu đãi đặc quyền <span>›</span></a>
        <a href="${cp}/page/news">Tạp chí đồng hồ <span>›</span></a>
        <c:choose><c:when test="${empty sessionScope.user}"><a href="${cp}/auth/login">Đăng nhập / Đăng ký <span>›</span></a></c:when><c:otherwise><a href="${cp}/page/profile">Tài khoản của tôi <span>›</span></a><a href="${cp}/orders/list">Đơn hàng <span>›</span></a></c:otherwise></c:choose>
        <div class="pro-drawer-support"><span>Cần tư vấn?</span><a href="tel:19006868">1900 6868</a></div>
    </div>
</div>
