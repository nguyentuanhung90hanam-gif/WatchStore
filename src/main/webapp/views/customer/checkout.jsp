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

    <c:if test="${not empty sessionScope.flash}">
        <div style="background:#fff5f5; border:1px solid #feb2b2; color:#9b2c2c; padding:12px 16px; border-radius:8px; margin-bottom:20px;">
            ${sessionScope.flash}
        </div>
        <c:remove var="flash" scope="session" />
    </c:if>
    
    <form action="${cp}/orders/place" method="post" class="checkout-layout">
        <section>
            <div class="checkout-card">
                <div class="card-heading">
                    <span>01</span>
                    <div>
                        <h2>Địa chỉ nhận hàng</h2>
                        <p>Chọn địa chỉ đã lưu trong tài khoản hoặc thêm địa chỉ mới.</p>
                    </div>
                </div>
                <c:choose>
                    <c:when test="${empty addresses}">
                        <p style="color:#e53e3e; margin-bottom:12px;">Bạn chưa có địa chỉ nhận hàng. Vui lòng thêm địa chỉ trước khi thanh toán.</p>
                        <a href="${cp}/page/address" class="button button-dark" style="display:inline-block; text-decoration:none; padding:8px 16px;">Thêm địa chỉ ngay</a>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${addresses}" var="a" varStatus="st">
                            <label class="payment-option ${a['default'] || st.first ? 'selected' : ''}" style="margin-bottom:10px;">
                                <input type="radio" name="addressId" value="${a.id}" ${a['default'] || st.first ? 'checked' : ''} required>
                                <div>
                                    <b>${a.name} · ${a.phone}</b>
                                    <small style="display:block; color:#718096; margin-top:2px;">${a.line}, ${a.ward}, ${a.district}, ${a.province}</small>
                                </div>
                            </label>
                        </c:forEach>
                        <div style="margin-top:12px;">
                            <a href="${cp}/page/address" style="color:#d4af37; font-size:14px; text-decoration:underline;">+ Quản lý hoặc thêm địa chỉ khác</a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
            
            <div class="checkout-card">
                <div class="card-heading">
                    <span>02</span>
                    <div>
                        <h2>Phương thức thanh toán</h2>
                        <p>Chọn phương thức thanh toán thuận tiện nhất cho bạn.</p>
                    </div>
                </div>
                <label class="payment-option selected" style="margin-bottom:10px;">
                    <input type="radio" name="payment" value="COD" checked>
                    <span>▣</span>
                    <div>
                        <b>Thanh toán khi nhận hàng (COD)</b>
                        <small>Nhận hàng, kiểm tra sản phẩm và thanh toán tiền mặt cho shipper.</small>
                    </div>
                </label>
                <label class="payment-option" style="margin-bottom:16px;">
                    <input type="radio" name="payment" value="BANK_TRANSFER">
                    <span>▤</span>
                    <div>
                        <b>Chuyển khoản ngân hàng</b>
                        <small>Chuyển khoản trực tiếp qua mã QR / số tài khoản ngân hàng của cửa hàng.</small>
                    </div>
                </label>

                <div class="card-heading" style="margin-top:24px; border-top:1px solid #edf2f7; padding-top:16px;">
                    <span>03</span>
                    <div>
                        <h2>Mã giảm giá & Ghi chú</h2>
                    </div>
                </div>
                <label style="display:block; margin-bottom:12px;">Mã ưu đãi (Voucher)
                    <input type="text" name="voucherCode" placeholder="Nhập mã voucher (nếu có)" style="width:100%; padding:10px; border:1px solid #cbd5e0; border-radius:6px; margin-top:4px;">
                </label>
                <label style="display:block;">Ghi chú đơn hàng
                    <textarea name="note" placeholder="Ghi chú cho nhân viên đóng gói / giao hàng (ví dụ: giao giờ hành chính...)" style="width:100%; padding:10px; border:1px solid #cbd5e0; border-radius:6px; margin-top:4px; height:80px;"></textarea>
                </label>
            </div>
        </section>
        
        <aside class="order-summary checkout-summary">
            <h2>Đơn hàng của bạn (${cartItems.size()} sản phẩm)</h2>
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
                        <small>Số lượng: ${item.quantity} · ${not empty item.variantName ? item.variantName : 'Tiêu chuẩn'}</small>
                    </div>
                    <span><fmt:formatNumber value="${item.lineTotal}" pattern="#,##0"/>₫</span>
                </div>
            </c:forEach>
            <p><span>Tạm tính</span><b><fmt:formatNumber value="${subtotal}" pattern="#,##0"/>₫</b></p>
            <p><span>Phí vận chuyển</span>
                <b>
                    <c:choose>
                        <c:when test="${shipping.signum() == 0}">Miễn phí</c:when>
                        <c:otherwise><fmt:formatNumber value="${shipping}" pattern="#,##0"/>₫</c:otherwise>
                    </c:choose>
                </b>
            </p>
            <p class="summary-total"><span>Tổng thanh toán</span><b><fmt:formatNumber value="${subtotal + shipping}" pattern="#,##0"/>₫</b></p>
            <button type="submit" class="button button-gold full" ${empty addresses ? 'disabled' : ''}>Xác nhận đặt hàng</button>
            <a class="continue-link" href="${cp}/cart/view" style="display:block; text-align:center; margin-top:12px; color:#718096; text-decoration:none;">← Quay lại giỏ hàng</a>
        </aside>
    </form>
</main>