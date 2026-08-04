<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not adminLayout}">
    <footer class="site-footer pro-footer">
        <div class="page-shell pro-footer-top">
            <div class="pro-footer-intro">
                <a class="logo pro-logo light" href="${cp}/page/home"><span>W</span><span class="pro-logo-copy"><b>WATCHSTORE</b><small>THE MEN'S WATCH ATELIER</small></span></a>
                <p>Không chỉ là nơi chọn mua đồng hồ, WatchStore là không gian dành cho những quý ông trân trọng giá trị, kỹ nghệ và phong cách sống riêng.</p>
                <div class="pro-socials"><a href="#" aria-label="Facebook">f</a><a href="#" aria-label="Instagram">ig</a><a href="#" aria-label="YouTube">▶</a><a href="#" aria-label="TikTok">t</a></div>
            </div>
            <div class="pro-footer-column"><b>Khám phá</b><a href="${cp}/page/products">Tất cả đồng hồ</a><a href="${cp}/page/products?sort=new">Bộ sưu tập mới</a><a href="${cp}/page/products?type=automatic">Đồng hồ cơ</a><a href="${cp}/page/vouchers">Ưu đãi hiện có</a></div>
            <div class="pro-footer-column"><b>Dịch vụ khách hàng</b><a href="#services">Chính sách bảo hành</a><a href="#services">Vận chuyển &amp; đổi trả</a><a href="${cp}/page/news">Cẩm nang sử dụng</a><a href="${cp}/page/profile">Kiểm tra tài khoản</a></div>
            <div class="pro-footer-column pro-footer-contact"><b>Liên hệ</b><a href="tel:19006868"><small>HOTLINE TƯ VẤN</small><strong>1900 6868</strong></a><a href="mailto:hello@watchstore.vn">hello@watchstore.vn</a><span>Showroom: Ninh Bình, Việt Nam</span><span>08:00–21:00 · Thứ 2–Chủ nhật</span></div>
        </div>
        <div class="page-shell pro-footer-bottom"><span>© 2026 WatchStore. All rights reserved.</span><div><a href="#">Điều khoản</a><a href="#">Bảo mật</a><span>Java Web MVC · JSP</span></div></div>
    </footer>
</c:if>
<script src="${cp}/assets/js/app.js?v=${assetVersion}"></script>
</body>
</html>
