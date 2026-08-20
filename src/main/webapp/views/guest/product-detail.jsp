<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="prodImg" value="${not empty product.imageUrl ? product.imageUrl : product.image}" />
<c:set var="avgRating" value="${product.ratingAverage > 0 ? product.ratingAverage : (product.rating > 0 ? product.rating : 5.0)}" />
<c:set var="countRating" value="${product.ratingCount != null ? product.ratingCount : fn:length(reviews)}" />

<main class="page-shell detail-page">
    <div class="breadcrumbs">
        <a href="${cp}/page/home">Trang chủ</a><span>›</span><a href="${cp}/page/products">Sản phẩm</a><span>›</span><b>${product.name}</b>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div style="background:#e6fffa; border:1px solid #38b2ac; color:#234e52; padding:12px 16px; border-radius:8px; margin-bottom:20px;">
            ${sessionScope.flash}
        </div>
        <c:remove var="flash" scope="session" />
    </c:if>
    
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
        </section>
        
        <section class="product-summary">
            <p class="eyebrow dark">${product.brand} · ${product.sku}</p>
            <h1>${product.name}</h1>
            <div class="rating-line">
                <span class="stars" style="color:#d4af37;">
                    <c:forEach begin="1" end="5" var="s">
                        <c:choose>
                            <c:when test="${s <= avgRating}">★</c:when>
                            <c:otherwise>☆</c:otherwise>
                        </c:choose>
                    </c:forEach>
                </span>
                <span><fmt:formatNumber value="${avgRating}" pattern="0.0" />/5 · ${countRating} đánh giá</span>
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
                <b>Tình trạng</b>
                <span style="color:#38a169; font-weight:600;">Còn hàng (Chính hãng 100%)</span>
            </div>
            <form class="buy-form" action="${cp}/cart/add" method="post">
                <input type="hidden" name="id" value="${product.id}">
                <input type="hidden" name="productId" value="${product.id}">
                <label>Số lượng
                    <input type="number" name="quantity" min="1" max="99" value="1">
                </label>
                <button type="submit" class="button button-dark">Thêm vào giỏ</button>
                <button type="submit" name="action" value="buy-now" class="button button-gold">Mua ngay</button>
            </form>
            <div class="product-benefits">
                <p>✓ Sản phẩm chính hãng 100%</p>
                <p>◷ Bảo hành 12 tháng chính hãng</p>
                <p>▱ Miễn phí giao hàng cho đơn từ 1.000.000₫</p>
            </div>
        </section>
    </div>
    
    <section class="product-tabs" style="margin-top:40px;">
        <div style="border-bottom: 2px solid #e2e8f0; margin-bottom: 24px;">
            <h2 style="font-size: 20px; font-weight:700; margin-bottom: 12px; color:#1a202c;">Thông tin sản phẩm & Đánh giá</h2>
        </div>
        
        <div style="margin-bottom: 36px;">
            <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 8px;">Mô tả sản phẩm</h3>
            <p style="color:#4a5568; line-height: 1.6;">
                <c:choose>
                    <c:when test="${not empty product.description}">${product.description}</c:when>
                    <c:when test="${not empty product.shortDescription}">${product.shortDescription}</c:when>
                    <c:otherwise>${product.name} được hoàn thiện tỉ mỉ từ vật liệu cao cấp, sở hữu bộ máy chính xác và thiết kế đẳng cấp dành cho người sành đồng hồ.</c:otherwise>
                </c:choose>
            </p>
        </div>

        <!-- REVIEWS SECTION -->
        <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 24px; margin-bottom: 36px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; flex-wrap:wrap; gap:12px;">
                <div>
                    <h3 style="font-size: 18px; font-weight: 700; color:#1a202c; margin:0;">
                        Đánh giá từ khách hàng (${fn:length(reviews)})
                    </h3>
                    <div style="display:flex; align-items:center; gap:8px; margin-top:4px;">
                        <span style="font-size: 20px; font-weight:700; color:#d4af37;"><fmt:formatNumber value="${avgRating}" pattern="0.0" /></span>
                        <span style="color:#d4af37; font-size:16px;">
                            <c:forEach begin="1" end="5" var="s">
                                <c:choose>
                                    <c:when test="${s <= avgRating}">★</c:when>
                                    <c:otherwise>☆</c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </span>
                        <span style="color:#718096; font-size:14px;">(${countRating} lượt đánh giá)</span>
                    </div>
                </div>
                <div>
                    <a href="${cp}/page/reviews" class="button button-outline" style="padding: 8px 16px; font-size:13px; text-decoration:none;">
                        Gửi đánh giá đơn hàng
                    </a>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty reviews}">
                    <p style="color:#718096; font-style:italic; text-align:center; padding:20px 0;">
                        Chưa có đánh giá nào cho sản phẩm này. Khách hàng đã mua có thể gửi đánh giá từ mục "Đơn hàng của tôi".
                    </p>
                </c:when>
                <c:otherwise>
                    <div style="display:grid; gap:16px;">
                        <c:forEach var="rev" items="${reviews}">
                            <div style="background:#fff; border:1px solid #edf2f7; border-radius:8px; padding:16px;">
                                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                                    <div style="display:flex; align-items:center; gap:8px;">
                                        <b style="color:#2d3748;">${not empty rev.userFullName ? rev.userFullName : 'Khách hàng ẩn danh'}</b>
                                        <span style="color:#38a169; font-size:12px; background:#f0fff4; padding:2px 6px; border-radius:4px;">✓ Đã mua hàng</span>
                                    </div>
                                    <span style="color:#a0aec0; font-size:12px;">
                                        <fmt:formatDate value="${rev.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                    </span>
                                </div>
                                <div style="color:#d4af37; margin-bottom:6px;">
                                    <c:forEach begin="1" end="5" var="s">
                                        <c:choose>
                                            <c:when test="${s <= rev.rating}">★</c:when>
                                            <c:otherwise>☆</c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </div>
                                <c:if test="${not empty rev.title}">
                                    <b style="display:block; color:#1a202c; margin-bottom:4px; font-size:14px;">${rev.title}</b>
                                </c:if>
                                <p style="color:#4a5568; margin:0; font-size:14px; line-height:1.5;">${rev.content}</p>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- COMMENTS SECTION -->
        <div style="background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 24px;">
            <h3 style="font-size: 18px; font-weight: 700; color:#1a202c; margin-bottom:16px;">
                Hỏi đáp & Bình luận (${fn:length(comments)})
            </h3>

            <!-- Comment Form -->
            <form action="${cp}/comments/add" method="post" style="margin-bottom:24px;">
                <input type="hidden" name="productId" value="${product.id}">
                <div style="display:flex; gap:12px; flex-direction:column;">
                    <textarea name="content" rows="3" placeholder="Đặt câu hỏi hoặc chia sẻ cảm nhận về sản phẩm này..." required
                              style="width:100%; border:1px solid #cbd5e0; border-radius:8px; padding:10px 14px; font-family:inherit; font-size:14px; resize:vertical;"></textarea>
                    <div style="display:flex; justify-content:flex-end;">
                        <button type="submit" class="button button-dark" style="padding:8px 20px; font-size:14px;">Gửi bình luận</button>
                    </div>
                </div>
            </form>

            <c:choose>
                <c:when test="${empty comments}">
                    <p style="color:#718096; font-style:italic; text-align:center; padding:12px 0;">
                        Chưa có bình luận nào. Hãy là người đầu tiên đặt câu hỏi!
                    </p>
                </c:when>
                <c:otherwise>
                    <div style="display:grid; gap:12px;">
                        <c:forEach var="c" items="${comments}">
                            <div style="border-bottom:1px solid #edf2f7; padding-bottom:12px;">
                                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;">
                                    <b style="color:#2d3748; font-size:14px;">${not empty c.userFullName ? c.userFullName : 'Khách hàng'}</b>
                                    <span style="color:#a0aec0; font-size:12px;">
                                        <fmt:formatDate value="${c.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                    </span>
                                </div>
                                <p style="color:#4a5568; margin:0; font-size:14px; line-height:1.5;">${c.content}</p>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>
