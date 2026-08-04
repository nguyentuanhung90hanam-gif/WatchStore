<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<main class="pro-home">
    <section class="hero hero-carousel pro-hero" data-hero-carousel data-autoplay="5500" aria-label="Bộ sưu tập nổi bật" tabindex="0">
        <article class="hero-slide active" aria-hidden="false">
            <div class="hero-media" style="--hero-image:url('${cp}/assets/images/hero-watchstore.png')" role="img" aria-label="Đồng hồ nam phong cách lịch lãm"></div>
            <div class="hero-scrim"></div>
            <div class="hero-content page-shell">
                <p class="eyebrow">THE HERITAGE COLLECTION · 2026</p>
                <h1>DẤU ẤN<br><em>THỜI GIAN</em></h1>
                <p class="hero-copy">Một chiếc đồng hồ được chọn đúng không chỉ hoàn thiện phong cách. Nó kể câu chuyện về bản lĩnh, trải nghiệm và những giá trị bạn theo đuổi.</p>
                <div class="hero-actions"><a class="button button-gold" href="${cp}/page/products">Khám phá bộ sưu tập <span>→</span></a><a class="button button-ghost" href="#signature">Câu chuyện WatchStore</a></div>
            </div>
            <div class="hero-note"><span>01</span><div><b>Heritage Selection</b><small>Vẻ đẹp vượt thời gian</small></div></div>
        </article>

        <article class="hero-slide" aria-hidden="true" inert>
            <div class="hero-media" style="--hero-image:url('${cp}/assets/images/automatic-collection.png')" role="img" aria-label="Bộ sưu tập đồng hồ cơ khí"></div>
            <div class="hero-scrim"></div>
            <div class="hero-content page-shell">
                <p class="eyebrow">MECHANICAL ARTISTRY</p>
                <h1>CHUYỂN ĐỘNG<br><em>TINH TẾ</em></h1>
                <p class="hero-copy">Khám phá vẻ đẹp của nghệ thuật cơ khí: nơi hàng trăm chi tiết nhỏ cùng vận hành để tạo nên một nhịp điệu chính xác.</p>
                <div class="hero-actions"><a class="button button-gold" href="${cp}/page/products?type=automatic">Xem đồng hồ cơ <span>→</span></a><a class="button button-ghost" href="${cp}/page/news">Tìm hiểu bộ máy</a></div>
            </div>
            <div class="hero-note"><span>02</span><div><b>Mechanical Artistry</b><small>Kỹ nghệ trong từng nhịp chuyển động</small></div></div>
        </article>

        <article class="hero-slide product-focus" aria-hidden="true" inert>
            <div class="hero-media" style="--hero-image:url('${cp}/assets/images/watch-3.png')" role="img" aria-label="Đồng hồ thể thao nam"></div>
            <div class="hero-scrim"></div>
            <div class="hero-content page-shell">
                <p class="eyebrow">MADE FOR THE JOURNEY</p>
                <h1>BẢN LĨNH<br><em>DẪN LỐI</em></h1>
                <p class="hero-copy">Thiết kế mạnh mẽ, bền bỉ và linh hoạt để đồng hành cùng quý ông từ phòng họp đến những hành trình xa.</p>
                <div class="hero-actions"><a class="button button-gold" href="${cp}/page/products?type=sport">Khám phá Sport Watch <span>→</span></a><a class="button button-ghost" href="${cp}/page/vouchers">Xem ưu đãi</a></div>
            </div>
            <div class="hero-note"><span>03</span><div><b>Sport Performance</b><small>Sẵn sàng cho mọi thử thách</small></div></div>
        </article>

        <div class="hero-controls" aria-label="Điều khiển banner">
            <button type="button" class="hero-arrow" data-hero-prev aria-label="Banner trước"><span aria-hidden="true">‹</span></button>
            <div class="hero-dots" data-hero-dots role="tablist" aria-label="Chọn banner"></div>
            <span class="hero-counter" data-hero-counter>01 / 03</span>
            <button type="button" class="hero-arrow" data-hero-next aria-label="Banner tiếp theo"><span aria-hidden="true">›</span></button>
        </div>
        <a class="pro-hero-scroll" href="#benefits"><span>SCROLL TO DISCOVER</span><i></i></a>
    </section>

    <section id="benefits" class="pro-benefits" aria-label="Cam kết của WatchStore">
        <div class="page-shell pro-benefit-grid">
            <article><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 2 4 6v6c0 5 3.4 8.7 8 10 4.6-1.3 8-5 8-10V6l-8-4Z"/><path d="m8.5 12 2.2 2.2 4.8-5"/></svg><div><b>100% chính hãng</b><span>Cam kết nguồn gốc minh bạch</span></div></article>
            <article><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2M8 3l-2 2M16 3l2 2"/></svg><div><b>Bảo hành uy tín</b><span>Hỗ trợ kỹ thuật toàn quốc</span></div></article>
            <article><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 7h11v10H3zM14 10h4l3 3v4h-7z"/><circle cx="7" cy="18" r="2"/><circle cx="17" cy="18" r="2"/></svg><div><b>Giao hàng miễn phí</b><span>Đóng gói an toàn, bảo mật</span></div></article>
            <article><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 12a8 8 0 1 0 3-6.2L4 8"/><path d="M4 3v5h5"/></svg><div><b>Đổi trả trong 7 ngày</b><span>An tâm trải nghiệm sản phẩm</span></div></article>
        </div>
    </section>

    <section class="pro-brandbar" aria-label="Các thương hiệu nổi bật">
        <div class="page-shell pro-brandbar-inner"><span>LONGINES</span><span>TISSOT</span><span>SEIKO</span><span>ORIENT</span><span>CITIZEN</span><span>CASIO</span></div>
    </section>

    <section class="section page-shell pro-category-section">
        <div class="pro-section-intro">
            <div><p class="eyebrow dark">CHỌN THEO PHONG CÁCH</p><h2>Một chiếc đồng hồ.<br><em>Một tuyên ngôn riêng.</em></h2></div>
            <p>Mỗi khoảnh khắc cần một dấu ấn khác biệt. Khám phá những lựa chọn được tuyển chọn dựa trên phong cách sống của quý ông hiện đại.</p>
        </div>
        <div class="pro-category-mosaic">
            <a class="pro-category-card pro-category-large" href="${cp}/page/products?type=automatic"><img src="${cp}/assets/images/automatic-collection.png" alt="Đồng hồ Automatic" loading="lazy"><span class="pro-image-shade"></span><div><small>MECHANICAL SOUL</small><h3>Automatic</h3><p>Kỹ nghệ cơ khí trong từng nhịp chuyển động.</p><b>Khám phá <i>→</i></b></div></a>
            <a class="pro-category-card" href="${cp}/page/products?type=dress"><img src="${cp}/assets/images/watch-1.png" alt="Đồng hồ thanh lịch" loading="lazy"><span class="pro-image-shade"></span><div><small>REFINED ESSENTIAL</small><h3>Thanh lịch</h3><b>Khám phá <i>→</i></b></div></a>
            <a class="pro-category-card" href="${cp}/page/products?type=sport"><img src="${cp}/assets/images/watch-3.png" alt="Đồng hồ thể thao" loading="lazy"><span class="pro-image-shade"></span><div><small>BUILT TO PERFORM</small><h3>Thể thao</h3><b>Khám phá <i>→</i></b></div></a>
            <a class="pro-category-card pro-category-wide" href="${cp}/page/products?type=quartz"><img src="${cp}/assets/images/hero-watchstore.png" alt="Đồng hồ Quartz" loading="lazy"><span class="pro-image-shade"></span><div><small>EVERYDAY PRECISION</small><h3>Quartz hiện đại</h3><b>Khám phá <i>→</i></b></div></a>
        </div>
    </section>

    <section class="section pro-products-section">
        <div class="page-shell">
            <div class="section-heading pro-products-heading">
                <div><p class="eyebrow dark">TUYỂN CHỌN DÀNH CHO QUÝ ÔNG</p><h2>Được yêu thích nhất</h2></div>
                <div class="pro-product-tabs" role="tablist" aria-label="Nhóm sản phẩm"><button class="active" type="button">Nổi bật</button><a href="${cp}/page/products?sort=new">Mới nhất</a><a href="${cp}/page/vouchers">Đang ưu đãi</a></div>
                <a class="pro-text-link" href="${cp}/page/products">Xem tất cả <span>→</span></a>
            </div>
            <div class="product-grid pro-product-grid">
                <c:forEach items="${featuredProducts}" var="product"><jsp:include page="/views/shared/product-card.jsp"><jsp:param name="productId" value="${product.id}" /></jsp:include></c:forEach>
            </div>
        </div>
    </section>

    <section id="signature" class="pro-editorial">
        <div class="pro-editorial-media"><img src="${cp}/assets/images/automatic-collection.png" alt="Nghệ thuật chế tác đồng hồ cơ" loading="lazy"><span>MECHANICAL<br>ARTISTRY</span></div>
        <div class="pro-editorial-content">
            <p class="eyebrow">WATCHSTORE SIGNATURE</p>
            <h2>Chuyển động tinh tế.<br><em>Giá trị vượt thời gian.</em></h2>
            <p>Đằng sau mỗi mặt số là một thế giới của kỹ thuật và sự kiên nhẫn. WatchStore tuyển chọn những thiết kế cân bằng giữa di sản chế tác, độ tin cậy và thẩm mỹ đương đại.</p>
            <div class="pro-editorial-stats"><div><b>80+</b><span>Thương hiệu tuyển chọn</span></div><div><b>10.000+</b><span>Khách hàng tin tưởng</span></div><div><b>5 năm</b><span>Đồng hành và bảo hành</span></div></div>
            <a class="button button-outline-light" href="${cp}/page/products?type=automatic">Khám phá Automatic <span>→</span></a>
        </div>
    </section>

    <section id="services" class="section page-shell pro-service-section">
        <div class="pro-centered-heading"><p class="eyebrow dark">TRẢI NGHIỆM KHÁC BIỆT</p><h2>Dịch vụ xứng tầm lựa chọn</h2><p>Từ tư vấn đến hậu mãi, mỗi điểm chạm đều được chăm chút để bạn an tâm tận hưởng giá trị của thời gian.</p></div>
        <div class="pro-service-cards">
            <article><span>01</span><svg viewBox="0 0 48 48" aria-hidden="true"><circle cx="24" cy="17" r="8"/><path d="M9 42c1-10 7-15 15-15s14 5 15 15M36 8l4 4-8 8"/></svg><h3>Tư vấn chuyên sâu</h3><p>Đội ngũ am hiểu giúp bạn chọn đúng kích thước, bộ máy và phong cách.</p><a href="tel:19006868">Đặt lịch tư vấn →</a></article>
            <article><span>02</span><svg viewBox="0 0 48 48" aria-hidden="true"><circle cx="24" cy="24" r="17"/><circle cx="24" cy="24" r="4"/><path d="M24 7v5M24 36v5M7 24h5M36 24h5"/></svg><h3>Chăm sóc chuyên nghiệp</h3><p>Quy trình kiểm tra, vệ sinh và bảo dưỡng cẩn trọng bởi kỹ thuật viên.</p><a href="${cp}/page/news">Xem hướng dẫn →</a></article>
            <article><span>03</span><svg viewBox="0 0 48 48" aria-hidden="true"><path d="M11 18 24 8l13 10v20H11z"/><path d="M18 38V25h12v13M8 18h32"/></svg><h3>Trải nghiệm showroom</h3><p>Không gian riêng tư để thử, cảm nhận và so sánh từng thiết kế.</p><a href="#showroom">Tìm showroom →</a></article>
        </div>
    </section>

    <section class="pro-campaign">
        <div class="page-shell pro-campaign-inner">
            <div class="pro-campaign-copy"><p class="eyebrow">WEEKEND PRIVATE SALE</p><h2>Đặc quyền dành cho<br><em>thành viên WatchStore</em></h2><p>Ưu đãi đến 25% cho các thiết kế tuyển chọn. Số lượng giới hạn trong thời gian chương trình.</p><a class="button button-gold" href="${cp}/page/vouchers">Nhận ưu đãi ngay <span>→</span></a></div>
            <div class="pro-countdown" data-campaign-countdown data-hours="47" aria-label="Thời gian còn lại">
                <div><b data-days>01</b><span>Ngày</span></div><i>:</i><div><b data-hours>23</b><span>Giờ</span></div><i>:</i><div><b data-minutes>59</b><span>Phút</span></div><i>:</i><div><b data-seconds>59</b><span>Giây</span></div>
            </div>
        </div>
    </section>

    <section class="section page-shell pro-journal">
        <div class="section-heading"><div><p class="eyebrow dark">THE WATCH JOURNAL</p><h2>Câu chuyện &amp; kiến thức</h2></div><a class="pro-text-link" href="${cp}/page/news">Đọc tất cả <span>→</span></a></div>
        <div class="pro-journal-grid">
            <article class="pro-journal-feature"><a href="${cp}/page/news"><img src="${cp}/assets/images/hero-watchstore.png" alt="Cách chọn đồng hồ nam" loading="lazy"></a><div><small>PHONG CÁCH · 8 PHÚT ĐỌC</small><h3><a href="${cp}/page/news">Cách chọn đồng hồ phù hợp với cổ tay và phong cách của quý ông</a></h3><p>Những nguyên tắc tinh gọn giúp chiếc đồng hồ trở thành điểm nhấn vừa đủ cho mọi bộ trang phục.</p><a href="${cp}/page/news">Đọc bài viết <span>→</span></a></div></article>
            <article><a href="${cp}/page/news"><img src="${cp}/assets/images/watch-2.png" alt="Phân biệt đồng hồ cơ và Quartz" loading="lazy"></a><div><small>KIẾN THỨC · 6 PHÚT ĐỌC</small><h3><a href="${cp}/page/news">Automatic hay Quartz: đâu là chuyển động dành cho bạn?</a></h3><a href="${cp}/page/news">Đọc bài viết <span>→</span></a></div></article>
            <article><a href="${cp}/page/news"><img src="${cp}/assets/images/watch-4.png" alt="Bảo quản đồng hồ" loading="lazy"></a><div><small>CHĂM SÓC · 5 PHÚT ĐỌC</small><h3><a href="${cp}/page/news">5 thói quen giúp chiếc đồng hồ bền đẹp cùng năm tháng</a></h3><a href="${cp}/page/news">Đọc bài viết <span>→</span></a></div></article>
        </div>
    </section>

    <section id="showroom" class="pro-showroom">
        <div class="pro-showroom-image"><img src="${cp}/assets/images/hero-watchstore.png" alt="Không gian showroom WatchStore" loading="lazy"></div>
        <div class="pro-showroom-content"><p class="eyebrow dark">WATCHSTORE SHOWROOM</p><h2>Chạm để cảm nhận.<br><em>Thử để thấu hiểu.</em></h2><p>Đến showroom để trực tiếp trải nghiệm chất liệu, độ hoàn thiện và nhận tư vấn riêng theo phong cách của bạn.</p><div class="pro-showroom-address"><b>SHOWROOM NINH BÌNH</b><span>Trung tâm thành phố Ninh Bình, Việt Nam</span><span>Mở cửa: 08:00–21:00 mỗi ngày</span></div><a class="button button-dark" href="tel:19006868">Đặt lịch trải nghiệm <span>→</span></a></div>
    </section>

    <section class="newsletter pro-newsletter"><div class="page-shell newsletter-inner"><div><p class="eyebrow">WATCHSTORE PRIVILEGE</p><h2>Gia nhập thế giới WatchStore</h2><span>Nhận thông tin về sản phẩm mới, bộ sưu tập giới hạn và đặc quyền dành riêng cho thành viên.</span></div><form><input type="email" aria-label="Địa chỉ email" placeholder="Nhập địa chỉ email của bạn"><button class="button button-gold" type="button" data-demo-toast="Đăng ký nhận tin thành công">Đăng ký <span>→</span></button></form></div></section>
</main>
