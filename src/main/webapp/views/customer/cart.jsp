<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<main class="page-shell cart-page">
    <div class="page-title compact">
        <p class="eyebrow dark">ĐƠN HÀNG CỦA BẠN</p>
        <h1>Giỏ hàng <small>(${cartItems.size()} sản phẩm)</small></h1>
    </div>
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
                                <p>Màu: Tiêu chuẩn · Chính hãng</p>
                                <b><fmt:formatNumber value="${not empty item.product.price ? item.product.price : item.price}" pattern="#,##0" />₫</b>
                            </div>
                            <form action="${cp}/cart/update" method="post" class="quantity-form">
                                <input type="hidden" name="id" value="${not empty item.product.id ? item.product.id : item.productId}">
                                <button type="button" data-quantity-minus>−</button>
                                <input name="quantity" value="${item.quantity}" min="1" type="number">
                                <button type="button" data-quantity-plus>+</button>
                                <button type="submit">Cập nhật</button>
                            </form>
                            <form action="${cp}/cart/remove" method="post">
                                <input type="hidden" name="id" value="${not empty item.product.id ? item.product.id : item.productId}">
                                <button type="submit" class="remove-button">×</button>
                            </form>
                        </article>
                    </c:forEach>
                </section>
                <aside class="order-summary">
                    <h2>Tóm tắt đơn hàng</h2>
                    <label>Mã ưu đãi
                        <div>
                            <input placeholder="Nhập mã voucher">
                            <button type="button" data-demo-toast="Đã áp dụng voucher demo">Áp dụng</button>
                        </div>
                    </label>
                    <p><span>Tạm tính</span><b><fmt:formatNumber value="${subtotal}" pattern="#,##0" />₫</b></p>
                    <p><span>Giảm giá</span><b class="success-text">−<fmt:formatNumber value="${discount}" pattern="#,##0" />₫</b></p>
                    <p><span>Phí vận chuyển</span><b><fmt:formatNumber value="${shipping}" pattern="#,##0" />₫</b></p>
                    <p class="summary-total"><span>Tổng cộng</span><b><fmt:formatNumber value="${subtotal - discount + shipping}" pattern="#,##0" />₫</b></p>
                    <a class="button button-gold full" href="${cp}/cart/checkout">Tiến hành thanh toán</a>
                    <a class="continue-link" href="${cp}/page/products">← Tiếp tục mua sắm</a>
                </aside>
            </div>
        </c:otherwise>
    </c:choose>
</main>
