<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<main class="page-shell checkout-page">
    <div class="checkout-steps">
        <span class="done">1 Giỏ hàng</span>
        <i></i>
        <span class="active">2 Thanh toán</span>
        <i></i>
        <span>3 Hoàn tất</span>
    </div>
    
    <form action="${cp}/orders/place" method="post" class="checkout-layout">
        <section>
            <div class="checkout-card">
                <div class="card-heading">
                    <span>01</span>
                    <div>
                        <h2>Địa chỉ nhận hàng</h2>
                        <p>Chọn địa chỉ đã lưu trong tài khoản.</p>
                    </div>
                </div>
                <c:choose>
                    <c:when test="${empty addresses}">
                        <p>Bạn chưa có địa chỉ nhận hàng. <a href="${cp}/page/address">Thêm địa chỉ</a></p>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${addresses}" var="a">
                            <label class="payment-option ${a['default'] ? 'selected' : ''}">
                                <input type="radio" name="addressId" value="${a.id}" ${a['default'] ? 'checked' : ''} required>
                                <div>
                                    <b>${a.name} · ${a.phone}</b>
                                    <small>${a.line}, ${a.ward}, ${a.district}, ${a.province}</small>
                                </div>
                            </label>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
            
            <div class="checkout-card">
                <div class="card-heading">
                    <span>02</span>
                    <div>
                        <h2>Thanh toán</h2>
                        <p>Chọn phương thức thanh toán.</p>
                    </div>
                </div>
                <label class="payment-option selected">
                    <input type="radio" name="payment" value="COD" checked>
                    <span>▣</span>
                    <div>
                        <b>Thanh toán khi nhận hàng</b>
                        <small>Thanh toán khi nhận được hàng.</small>
                    </div>
                </label>
                <label class="payment-option">
                    <input type="radio" name="payment" value="BANK_TRANSFER">
                    <span>▤</span>
                    <div>
                        <b>Chuyển khoản ngân hàng</b>
                        <small>Đơn hàng được ghi nhận chờ thanh toán.</small>
                    </div>
                </label>
                <label>Ghi chú
                    <textarea name="note" placeholder="Ghi chú cho nhân viên giao hàng"></textarea>
                </label>
            </div>
        </section>
        
        <aside class="order-summary checkout-summary">
            <h2>Đơn hàng của bạn</h2>
            <c:forEach items="${cartItems}" var="item">
                <c:set var="itemImg" value="${not empty item.product.imageUrl ? item.product.imageUrl : (not empty item.product.image ? item.product.image : item.image)}" />
                <div class="checkout-product">
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
                    <div>
                        <b>${not empty item.product.name ? item.product.name : item.name}</b>
                        <small>Số lượng: ${item.quantity}</small>
                    </div>
                    <span><fmt:formatNumber value="${item.lineTotal}" pattern="#,##0"/>₫</span>
                </div>
            </c:forEach>
            <p><span>Tạm tính</span><b><fmt:formatNumber value="${subtotal}" pattern="#,##0"/>₫</b></p>
            <p><span>Phí vận chuyển</span><b><fmt:formatNumber value="${shipping}" pattern="#,##0"/>₫</b></p>
            <p class="summary-total"><span>Tổng cộng</span><b><fmt:formatNumber value="${subtotal + shipping}" pattern="#,##0"/>₫</b></p>
            <button type="submit" class="button button-gold full" ${empty addresses ? 'disabled' : ''}>Đặt hàng</button>
        </aside>
    </form>
</main>