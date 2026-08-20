<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<main class="page-shell account-page">
    <jsp:include page="/views/shared/account-nav.jsp" />
    <section class="account-content">
        <div class="account-heading">
            <div>
                <p class="eyebrow dark">LỊCH SỬ MUA HÀNG</p>
                <h1>Đơn hàng của tôi</h1>
            </div>
        </div>

        <c:if test="${not empty sessionScope.flash}">
            <div style="background:#e6fffa; border:1px solid #38b2ac; color:#234e52; padding:12px 16px; border-radius:8px; margin-bottom:20px; font-size:14px;">
                ${sessionScope.flash}
            </div>
            <c:remove var="flash" scope="session"/>
        </c:if>

        <c:choose>
            <c:when test="${empty orders}">
                <div style="text-align:center; padding:40px 20px; background:#f8fafc; border-radius:12px; border:1px dashed #cbd5e0;">
                    <p style="font-size:18px; color:#4a5568; margin-bottom:12px;">Bạn chưa có đơn hàng nào tại WatchStore.</p>
                    <a href="${cp}/page/products" class="button button-gold" style="display:inline-block; text-decoration:none;">Khám phá sản phẩm</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="customer-orders">
                    <c:forEach items="${orders}" var="order">
                        <article style="border:1px solid #e2e8f0; border-radius:10px; padding:16px; margin-bottom:16px; background:#fff;">
                            <div class="order-head" style="display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid #edf2f7; padding-bottom:10px; margin-bottom:12px;">
                                <div>
                                    <b style="font-size:16px; color:#1a202c;">#${order.code}</b>
                                    <small style="display:block; color:#718096; margin-top:2px;">
                                        <fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                    </small>
                                </div>
                                <div>
                                    <span class="status-badge" style="padding:4px 10px; border-radius:6px; font-size:12px; font-weight:600;
                                        <c:choose>
                                            <c:when test="${order.status == 'PENDING'}">background:#feebc8; color:#7b341e;</c:when>
                                            <c:when test="${order.status == 'CONFIRMED'}">background:#bee3f8; color:#2c5282;</c:when>
                                            <c:when test="${order.status == 'SHIPPING'}">background:#e9d8fd; color:#553c9a;</c:when>
                                            <c:when test="${order.status == 'COMPLETED'}">background:#c6f6d5; color:#22543d;</c:when>
                                            <c:when test="${order.status == 'CANCELLED'}">background:#fed7d7; color:#742a2a;</c:when>
                                            <c:otherwise>background:#edf2f7; color:#4a5568;</c:otherwise>
                                        </c:choose>
                                    ">
                                        <c:choose>
                                            <c:when test="${order.status == 'PENDING'}">Chờ xác nhận</c:when>
                                            <c:when test="${order.status == 'CONFIRMED'}">Đã xác nhận</c:when>
                                            <c:when test="${order.status == 'SHIPPING'}">Đang giao hàng</c:when>
                                            <c:when test="${order.status == 'COMPLETED'}">Hoàn thành</c:when>
                                            <c:when test="${order.status == 'CANCELLED'}">Đã hủy</c:when>
                                            <c:otherwise>${order.status}</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </div>
                            <div class="order-body" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
                                <div>
                                    <p style="margin:0; color:#4a5568; font-size:14px;">Địa chỉ giao: ${order.shippingAddress}</p>
                                </div>
                                <strong style="font-size:16px; color:#d4af37;">
                                    <fmt:formatNumber value="${order.totalPrice}" pattern="#,##0" />₫
                                </strong>
                            </div>
                            <div class="order-actions" style="display:flex; justify-content:flex-end; gap:8px; border-top:1px solid #edf2f7; padding-top:10px;">
                                <a href="${cp}/orders/detail?code=${order.code}" class="button button-outline" style="text-decoration:none; padding:6px 14px; font-size:13px;">Xem chi tiết</a>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>
