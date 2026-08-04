<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:forEach items="${featuredProducts}" var="cardProduct"><c:if test="${cardProduct.id == param.productId}">
    <article class="product-card pro-product-card">
        <div class="product-media">
            <c:if test="${not empty cardProduct.badge}"><span class="product-badge">${cardProduct.badge}</span></c:if>
            <a class="wish-button" href="${cp}/page/wishlist" aria-label="Thêm ${cardProduct.name} vào yêu thích">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.8-7.5 1.1-1.1a5.5 5.5 0 0 0-.1-7.8Z"/></svg>
            </a>
            <a class="product-image-link" href="${cp}/page/product?id=${cardProduct.id}"><img src="${cp}/assets/images/${cardProduct.image}" alt="${cardProduct.name}" loading="lazy"></a>
            <form action="${cp}/cart/add" method="post"><input type="hidden" name="id" value="${cardProduct.id}"><button class="quick-add"><span>+</span> Thêm vào giỏ hàng</button></form>
        </div>
        <div class="product-info">
            <div class="pro-product-brand"><p>${cardProduct.brand}</p><span class="stars">★★★★★ <small>(${cardProduct.rating})</small></span></div>
            <h3><a href="${cp}/page/product?id=${cardProduct.id}">${cardProduct.name}</a></h3>
            <small class="pro-product-spec">Nam · Kính sapphire · Chính hãng</small>
            <div class="price"><b><fmt:formatNumber value="${cardProduct.price}" pattern="#,##0" />₫</b><del><fmt:formatNumber value="${cardProduct.oldPrice}" pattern="#,##0" />₫</del><c:if test="${cardProduct.discountPercent > 0}"><em>-${cardProduct.discountPercent}%</em></c:if></div>
            <p class="pro-installment">Trả góp 0% · Bảo hành chính hãng</p>
        </div>
    </article>
</c:if></c:forEach>
