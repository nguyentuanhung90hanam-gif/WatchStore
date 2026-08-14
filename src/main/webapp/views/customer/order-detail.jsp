<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<main class="page-shell content-page order-detail">
<div class="breadcrumbs"><a href="${cp}/orders/list">Đơn hàng</a><span>›</span><b>#${order.code}</b></div>
<div class="order-detail-head"><div><p class="eyebrow dark">MÃ ĐƠN #${order.code}</p><h1>Chi tiết đơn hàng</h1><span>Đặt lúc ${order.createdAt}</span></div><span class="status-badge ${order.statusEnum.cssClass}">${order.statusEnum.label}</span></div>
<div class="order-detail-grid"><section><article class="detail-card"><h2>Sản phẩm</h2><c:forEach items="${orderItems}" var="item"><div class="order-product"><img src="${cp}/assets/images/${item.image}" alt=""><div><b>${item.productName}</b><span>${item.variantName} · ×${item.quantity}</span></div><strong><fmt:formatNumber value="${item.lineTotal}" pattern="#,##0"/>₫</strong></div></c:forEach></article>
<article class="detail-card"><h2>Địa chỉ nhận hàng</h2><b>${order.customerName}</b><p>${order.phone}</p><p>${order.shippingAddress}</p></article></section>
<aside class="detail-card payment-summary"><h2>Thanh toán</h2><p><span>Tổng đơn hàng</span><b><fmt:formatNumber value="${order.total}" pattern="#,##0"/>₫</b></p><p><span>Trạng thái thanh toán</span><b>${order.paymentStatus}</b></p>
<c:if test="${order.status == 'PENDING' || order.status == 'CONFIRMED'}"><form action="${cp}/orders/cancel" method="post"><input type="hidden" name="orderId" value="${order.id}"><label>Lý do hủy<textarea name="reason" required placeholder="Nhập lý do hủy đơn"></textarea></label><button class="button button-dark full" onclick="return confirm('Bạn chắc chắn muốn hủy đơn?')">Hủy đơn hàng</button></form></c:if>
</aside></div></main>