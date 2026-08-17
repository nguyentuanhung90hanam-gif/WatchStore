<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="prodImg" value="${not empty product.imageUrl ? product.imageUrl : product.image}" />

<main class="page-shell detail-page">
    <div class="breadcrumbs">
        <a href="${cp}/page/home">Trang chủ</a><span>›</span><a href="${cp}/page/products">Sản phẩm</a><span>›</span><b>${product.name}</b>
    </div>
    
    <div class="detail-grid">
        <section class="gallery">
            <div class="main-product-image">
                <c:if test="${not empty product.badge}">
                    <span class="product-badge">${product.badge}</span>
                </c:if>
                <c:choose>
                    <c:when test="${fn:startsWith(prodImg, 'http://') || fn:startsWith(prodImg, 'https://')}">
                        <img src="${prodImg}" alt="${product.name}"
                             onerror="this.onerror=null;this.src='${cp}/assets/images/watch-1.png';">
                    </c:when>
                    <c:when test="${fn:startsWith(prodImg, '/')}">
                        <img src="${cp}${prodImg}" alt="${product.name}"
                             onerror="this.onerror=null;this.src='${cp}/assets/images/watch-1.png';">
                    </c:when>
                    <c:otherwise>
                        <img src="${cp}/assets/images/${prodImg}" alt="${product.name}"
                             onerror="this.onerror=null;this.src='${cp}/assets/images/watch-1.png';">
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="thumbs">
                <button type="button">
                    <c:choose>
                        <c:when test="${fn:startsWith(prodImg, 'http://') || fn:startsWith(prodImg, 'https://')}">
                            <img src="${prodImg}" alt="${product.name}" onerror="this.onerror=null;this.src='${cp}/assets/images/watch-1.png';">
                        </c:when>
                        <c:when test="${fn:startsWith(prodImg, '/')}">
                            <img src="${cp}${prodImg}" alt="${product.name}" onerror="this.onerror=null;this.src='${cp}/assets/images/watch-1.png';">
                        </c:when>
                        <c:otherwise>
                            <img src="${cp}/assets/images/${prodImg}" alt="${product.name}" onerror="this.onerror=null;this.src='${cp}/assets/images/watch-1.png';">
                        </c:otherwise>
                    </c:choose>
                </button>
                <button type="button"><img src="${cp}/assets/images/watch-2.png" alt=""></button>
                <button type="button"><img src="${cp}/assets/images/watch-3.png" alt=""></button>
            </div>
        </section>
        
        <section class="product-summary">
            <p class="eyebrow dark">${product.brand} · ${product.sku}</p>
            <h1>${product.name}</h1>
            <div class="rating-line">
                <span class="stars">★★★★★</span>
                <span>${product.rating}/5 · 48 đánh giá · Đã bán 126</span>
            </div>
            <div class="detail-price">
                <b><fmt:formatNumber value="${product.price}" pattern="#,##0" />₫</b>
                <c:if test="${not empty product.oldPrice && product.oldPrice.signum() > 0}">
                    <del><fmt:formatNumber value="${product.oldPrice}" pattern="#,##0" />₫</del>
                </c:if>
                <c:if test="${product.discountPercent > 0}">
                    <em>-${product.discountPercent}%</em>
                </c:if>
            </div>
            <div class="option-block">
                <b>Màu mặt</b>
                <div class="swatches">
                    <button type="button" class="selected">Xanh navy</button>
                    <button type="button">Đen</button>
                    <button type="button">Bạc</button>
                </div>
            </div>
            <div class="option-block">
                <b>Dây đeo</b>
                <div class="swatches">
                    <button type="button" class="selected">Thép không gỉ</button>
                    <button type="button">Dây da</button>
                    <button type="button" disabled>Mesh · Hết hàng</button>
                </div>
            </div>
            <form class="buy-form" action="${cp}/cart/add" method="post">
                <input type="hidden" name="id" value="${product.id}">
                <label>Số lượng
                    <input type="number" name="quantity" min="1" max="${product.stock}" value="1">
                </label>
                <button type="submit" class="button button-dark">Thêm vào giỏ</button>
                <button type="submit" class="button button-gold" formaction="${cp}/cart/add">Mua ngay</button>
            </form>
            <div class="product-benefits">
                <p>✓ Sản phẩm chính hãng 100%</p>
                <p>◷ Bảo hành 24 tháng</p>
                <p>▱ Miễn phí giao hàng toàn quốc</p>
                <p>↺ Đổi trả trong vòng 7 ngày</p>
            </div>
        </section>
    </div>
    
    <section class="product-tabs">
        <nav>
            <button type="button" class="active">Mô tả</button>
            <button type="button">Thông số kỹ thuật</button>
            <button type="button">Đánh giá (48)</button>
        </nav>
        <div>
            <h2>Tinh thần hiện đại trong từng chi tiết</h2>
            <p>${product.name} kết hợp kỹ thuật chế tác chính xác với thiết kế dành riêng cho phong cách nam giới hiện đại. Sản phẩm phù hợp khi đi làm, gặp gỡ đối tác hoặc tham dự sự kiện.</p>
            <div class="spec-grid">
                <span><b>Loại máy</b>Automatic / Quartz cao cấp</span>
                <span><b>Kính</b>Sapphire chống trầy</span>
                <span><b>Chống nước</b>10 ATM</span>
                <span><b>Kích thước</b>40 mm</span>
            </div>
        </div>
    </section>
</main>
