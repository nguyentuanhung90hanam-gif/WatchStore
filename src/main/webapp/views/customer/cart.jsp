<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<main class="page-shell cart-page">
<div class="page-title compact"><p class="eyebrow dark">ĐƠN HÀNG CỦA BẠN</p><h1>Giỏ hàng <small>(${cartCount} sản phẩm)</small></h1></div>
<c:choose>
<c:when test="${empty cartItems}"><section class="empty-state"><span>▢</span><h2>Giỏ hàng đang trống</h2><p>Hãy khám phá bộ sưu tập đồng hồ được tuyển chọn cho bạn.</p><a class="button button-gold" href="${cp}/page/products">Tiếp tục mua sắm</a></section></c:when>
<c:otherwise><div class="cart-layout"><section class="cart-items">
<c:forEach items="${cartItems}" var="item"><article class="cart-item">
<img src="${cp}/assets/images/${item.image}" alt="${item.name}">
<div class="cart-item-info"><small>${item.brand} · ${item.sku}</small><h3>${item.name}</h3><p>${item.variantName}</p><b><fmt:formatNumber value="${item.price}" pattern="#,##0"/>₫</b></div>
<form action="${cp}/cart/update" method="post" class="quantity-form"><input type="hidden" name="variantId" value="${item.variantId}"><button type="button" data-quantity-minus>−</button><input name="quantity" value="${item.quantity}" min="0" max="${item.available}" type="number"><button type="button" data-quantity-plus>+</button><button>Cập nhật</button></form>
<form action="${cp}/cart/remove" method="post"><input type="hidden" name="variantId" value="${item.variantId}"><button class="remove-button" title="Xóa">×</button></form>
</article></c:forEach>
</section><aside class="order-summary"><h2>Tóm tắt đơn hàng</h2><p><span>Tạm tính</span><b><fmt:formatNumber value="${subtotal}" pattern="#,##0"/>₫</b></p><p><span>Giảm giá</span><b class="success-text">−<fmt:formatNumber value="${discount}" pattern="#,##0"/>₫</b></p><p><span>Phí vận chuyển</span><b><fmt:formatNumber value="${shipping}" pattern="#,##0"/>₫</b></p><p class="summary-total"><span>Tổng cộng</span><b><fmt:formatNumber value="${subtotal - discount + shipping}" pattern="#,##0"/>₫</b></p><a class="button button-gold full" href="${cp}/cart/checkout">Tiến hành thanh toán</a><a class="continue-link" href="${cp}/page/products">← Tiếp tục mua sắm</a></aside></div></c:otherwise></c:choose>
</main>