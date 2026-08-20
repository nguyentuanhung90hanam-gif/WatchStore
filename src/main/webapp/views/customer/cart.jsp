<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<main class="page-shell cart-page">
    <div class="page-title compact">
        <p class="eyebrow dark">ĐƠN HÀNG CỦA BẠN</p>
        <h1>Giỏ hàng <small>(${cartItems.size()} sản phẩm)</small></h1>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div style="background:#e6fffa; border:1px solid #38b2ac; color:#234e52; padding:12px 16px; border-radius:8px; margin-bottom:20px;">
            ${sessionScope.flash}
        </div>
        <c:remove var="flash" scope="session" />
    </c:if>

    <c:choose>
        <c:when test="${empty cartItems}">
            <section class="empty-state">
                <span>▢</span>
                <h2>Giỏ hàng đang trống</h2>
                <p>Hãy khám phá bộ sưu tập đồng hồ được tuyển chọn cho bạn.</p>
                <a class="button button-gold" href="${cp}/page/products">Tiếp tục mua sắm</a>
            </section>
        </c:when>
        <c:otherwise>
            <div class="cart-layout">
                <section class="cart-items">
                    <c:forEach items="${cartItems}" var="item">
                        <c:set var="itemImg" value="${not empty item.product.imageUrl ? item.product.imageUrl : (not empty item.product.image ? item.product.image : item.image)}" />
                        <c:set var="targetVarId" value="${not empty item.variantId ? item.variantId : (not empty item.product.id ? item.product.id : item.productId)}" />
                        <article class="cart-item">
                            <c:choose>
                                <c:when test="${fn:startsWith(itemImg, 'http://') || fn:startsWith(itemImg, 'https://')}">
                                    <img src="${itemImg}" alt="${not empty item.product.name ? item.product.name : item.name}"
                                         onerror="this.onerror=null;this.src='${cp}/assets/images/watch-1.png';">
                                </c:when>
                                <c:when test="${fn:startsWith(itemImg, '/')}">
                                    <img src="${cp}${itemImg}" alt="${not empty item.product.name ? item.product.name : item.name}"
                                         onerror="this.onerror=null;this.src='${cp}/assets/images/watch-1.png';">
                                </c:when>
                                <c:otherwise>
                                    <img src="${cp}/assets/images/${itemImg}" alt="${not empty item.product.name ? item.product.name : item.name}"
                                         onerror="this.onerror=null;this.src='${cp}/assets/images/watch-1.png';">
                                </c:otherwise>
                            </c:choose>
                            <div class="cart-item-info">
                                <small>${not empty item.product.brand ? item.product.brand : item.brand} · ${not empty item.product.sku ? item.product.sku : item.sku}</small>
                                <h3>${not empty item.product.name ? item.product.name : item.name}</h3>
                                <p>Biến thể: ${not empty item.variantName ? item.variantName : 'Tiêu chuẩn'} · Chính hãng</p>
                                <b><fmt:formatNumber value="${not empty item.product.price ? item.product.price : item.price}" pattern="#,##0" />₫</b>
                            </div>
                            <form action="${cp}/cart/update" method="post" class="quantity-form">
                                <input type="hidden" name="variantId" value="${targetVarId}">
                                <input type="hidden" name="id" value="${targetVarId}">
                                <button type="button" data-quantity-minus>−</button>
                                <input name="quantity" value="${item.quantity}" min="1" max="99" type="number">
                                <button type="button" data-quantity-plus>+</button>
                                <button type="submit">Cập nhật</button>
                            </form>
                            <form action="${cp}/cart/remove" method="post">
                                <input type="hidden" name="variantId" value="${targetVarId}">
                                <input type="hidden" name="id" value="${targetVarId}">
                                <button type="submit" class="remove-button" title="Xóa khỏi giỏ">×</button>
                            </form>
                        </article>
                    </c:forEach>
                </section>
                <aside class="order-summary">
                    <h2>Tóm tắt đơn hàng</h2>
                    <p><span>Tạm tính</span><b><fmt:formatNumber value="${subtotal}" pattern="#,##0" />₫</b></p>
                    <p><span>Phí vận chuyển</span>
                        <b>
                            <c:choose>
                                <c:when test="${shipping.signum() == 0}">Miễn phí</c:when>
                                <c:otherwise><fmt:formatNumber value="${shipping}" pattern="#,##0" />₫</c:otherwise>
                            </c:choose>
                        </b>
                    </p>
                    <p class="summary-total"><span>Tổng cộng</span><b><fmt:formatNumber value="${subtotal + shipping}" pattern="#,##0" />₫</b></p>
                    <a class="button button-gold full" href="${cp}/cart/checkout">Tiến hành thanh toán</a>
                    <a class="continue-link" href="${cp}/page/products">← Tiếp tục mua sắm</a>
                </aside>
            </div>
        </c:otherwise>
    </c:choose>
</main>
