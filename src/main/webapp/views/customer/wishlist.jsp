<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<main class="page-shell account-page">
    <jsp:include page="/views/shared/account-nav.jsp" />
    <section class="account-content">
        <div class="account-heading">
            <div>
                <p class="eyebrow dark">BỘ SƯU TẬP CỦA BẠN</p>
                <h1>Sản phẩm yêu thích</h1>
            </div>
            <span>${wishlistProducts.size()} sản phẩm</span>
        </div>

        <c:if test="${not empty sessionScope.flash}">
            <div class="form-alert success" style="margin-bottom: 20px;">
                ${sessionScope.flash}
            </div>
            <c:remove var="flash" scope="session"/>
        </c:if>

        <c:choose>
            <c:when test="${empty wishlistProducts}">
                <div style="text-align: center; padding: 40px 20px; background: #fff; border-radius: 12px; border: 1px solid #eee;">
                    <p style="font-size: 16px; color: #666; margin-bottom: 15px;">Danh sách sản phẩm yêu thích của bạn hiện đang trống.</p>
                    <a href="${cp}/page/products" class="button button-gold">Khám phá sản phẩm ngay</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="product-grid account-products pro-product-grid">
                    <c:forEach items="${wishlistProducts}" var="product">
                        <article class="product-card pro-product-card">
                            <div class="product-media">
                                <form action="${cp}/wishlist" method="post" style="display:inline;">
                                    <input type="hidden" name="productId" value="${product.id}">
                                    <button class="wish-button liked" type="submit" aria-label="Xóa ${product.name} khỏi yêu thích">
                                        <svg viewBox="0 0 24 24" aria-hidden="true">
                                            <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.8-7.5 1.1-1.1a5.5 5.5 0 0 0-.1-7.8Z"/>
                                        </svg>
                                    </button>
                                </form>
                                <a class="product-image-link" href="${cp}/page/product?id=${product.id}">
                                    <img src="${cp}/assets/images/${product.image}" alt="${product.name}" loading="lazy">
                                </a>
                            </div>
                            <div class="product-info">
                                <div class="pro-product-brand">
                                    <p>${product.brand}</p>
                                    <span class="stars">★★★★★</span>
                                </div>
                                <h3><a href="${cp}/page/product?id=${product.id}">${product.name}</a></h3>
                                <div class="price">
                                    <b><fmt:formatNumber value="${product.price}" pattern="#,##0" />₫</b>
                                </div>
                                <form action="${cp}/cart/add" method="post">
                                    <input type="hidden" name="id" value="${product.id}">
                                    <button class="button button-dark full">Thêm vào giỏ</button>
                                </form>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>
