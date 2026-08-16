<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<main class="page-shell content-page order-detail">
    <div class="breadcrumbs"><a href="${cp}/orders/list">Đơn hàng</a><span>›</span><b>#${order.code}</b></div>
    <div class="order-detail-head">
        <div>
            <p class="eyebrow dark">MÃ ĐƠN #${order.code}</p>
            <h1>Chi tiết đơn hàng</h1>
            <span>Đặt lúc ${order.createdAt}</span>
        </div>
        <span class="status-badge ${order.statusEnum.cssClass}">${order.statusEnum.label}</span>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div style="background:#d1fae5; border:1px solid #6ee7b7; color:#065f46; padding:12px 16px; border-radius:8px; margin-bottom:20px; font-size:14px;">
            ✅ ${sessionScope.flash}
        </div>
        <c:remove var="flash" scope="session"/>
    </c:if>

    <div class="order-detail-grid">
        <section>
            <article class="detail-card">
                <h2>Sản phẩm</h2>
                <c:forEach items="${orderItems}" var="item">
                    <div class="order-product" style="display:flex; justify-content:space-between; align-items:center; padding:12px 0; border-bottom:1px solid #eee;">
                        <div>
                            <b>${item.productName}</b>
                            <div style="font-size:13px; color:#666;">${item.variantName} · Số lượng: ${item.quantity}</div>
                        </div>
                        <strong><fmt:formatNumber value="${item.lineTotal}" pattern="#,##0"/>₫</strong>
                    </div>
                </c:forEach>
            </article>

            <article class="detail-card">
                <h2>Địa chỉ nhận hàng</h2>
                <b>${order.customerName}</b>
                <p>${order.phone}</p>
                <p>${order.shippingAddress}</p>
            </article>
        </section>

        <aside class="detail-card payment-summary">
            <h2>Thanh toán</h2>
            <p><span>Tổng đơn hàng</span><b><fmt:formatNumber value="${order.total}" pattern="#,##0"/>₫</b></p>
            <p><span>Trạng thái thanh toán</span><b>${order.paymentStatus}</b></p>

            <c:if test="${order.status == 'PENDING' || order.status == 'CONFIRMED'}">
                <hr>
                <form action="${cp}/orders/cancel" method="post">
                    <input type="hidden" name="orderId" value="${order.id}">
                    <label style="display:block; margin-bottom:8px; font-weight:600;">Lý do hủy
                        <textarea name="reason" required placeholder="Nhập lý do hủy đơn" style="width:100%; border:1px solid #ccc; border-radius:4px; padding:8px;"></textarea>
                    </label>
                    <button class="button button-dark full" style="width:100%; padding:10px; background:#111; color:#fff; border:none; border-radius:6px; font-weight:600;" onclick="return confirm('Bạn chắc chắn muốn hủy đơn?')">Hủy đơn hàng</button>
                </form>
            </c:if>

            <c:if test="${order.status == 'COMPLETED'}">
                <hr>
                <h3 style="font-size:15px; font-weight:700; margin-bottom:12px;">Yêu cầu hỗ trợ</h3>
                <details style="margin-bottom:12px; background:#f9f9f9; padding:10px; border-radius:6px; border:1px solid #eee;">
                    <summary style="cursor:pointer; font-weight:600; color:#d4af37;">🔄 Gửi yêu cầu đổi trả</summary>
                    <form action="${cp}/orders/return" method="post" style="margin-top:10px;">
                        <input type="hidden" name="orderId" value="${order.id}">
                        <label style="display:block; margin-bottom:8px; font-size:13px; font-weight:600;">Chọn sản phẩm:
                            <select name="productName" required style="width:100%; padding:6px; margin-top:4px; border:1px solid #ccc; border-radius:4px;">
                                <c:forEach items="${orderItems}" var="item">
                                    <option value="${item.productName}">${item.productName} (${item.variantName})</option>
                                </c:forEach>
                            </select>
                        </label>
                        <label style="display:block; margin-bottom:8px; font-size:13px; font-weight:600;">Lý do đổi trả:
                            <textarea name="reason" required placeholder="Nhập chi tiết lý do đổi trả..." style="width:100%; padding:6px; margin-top:4px; border:1px solid #ccc; border-radius:4px;"></textarea>
                        </label>
                        <button type="submit" style="width:100%; padding:8px; background:#d4af37; color:#fff; border:none; border-radius:4px; font-weight:600; cursor:pointer;">Gửi yêu cầu đổi trả</button>
                    </form>
                </details>

                <details style="background:#f9f9f9; padding:10px; border-radius:6px; border:1px solid #eee;">
                    <summary style="cursor:pointer; font-weight:600; color:#2563eb;">🛡 Gửi yêu cầu bảo hành</summary>
                    <form action="${cp}/orders/warranty" method="post" style="margin-top:10px;">
                        <input type="hidden" name="orderId" value="${order.id}">
                        <label style="display:block; margin-bottom:8px; font-size:13px; font-weight:600;">Chọn sản phẩm:
                            <select name="productName" required style="width:100%; padding:6px; margin-top:4px; border:1px solid #ccc; border-radius:4px;">
                                <c:forEach items="${orderItems}" var="item">
                                    <option value="${item.productName}">${item.productName} (${item.variantName})</option>
                                </c:forEach>
                            </select>
                        </label>
                        <label style="display:block; margin-bottom:8px; font-size:13px; font-weight:600;">Mô tả lỗi sản phẩm:
                            <textarea name="note" required placeholder="Mô tả hiện trạng lỗi/sự cố của đồng hồ..." style="width:100%; padding:6px; margin-top:4px; border:1px solid #ccc; border-radius:4px;"></textarea>
                        </label>
                        <button type="submit" style="width:100%; padding:8px; background:#2563eb; color:#fff; border:none; border-radius:4px; font-weight:600; cursor:pointer;">Gửi yêu cầu bảo hành</button>
                    </form>
                </details>
            </c:if>
        </aside>
    </div>
</main>