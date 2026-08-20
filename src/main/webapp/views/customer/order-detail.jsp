<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<main class="page-shell content-page order-detail">
    <div class="breadcrumbs"><a href="${cp}/orders/list">Đơn hàng</a><span>›</span><b>#${order.code}</b></div>
    <div class="order-detail-head">
        <div>
            <p class="eyebrow dark">MÃ ĐƠN #${order.code}</p>
            <h1>Chi tiết đơn hàng</h1>
            <span>Đặt lúc <fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm" /></span>
        </div>
        <span class="status-badge" style="padding:6px 14px; border-radius:6px; font-size:13px; font-weight:700;
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

    <c:if test="${not empty sessionScope.flash}">
        <div style="background:#e6fffa; border:1px solid #38b2ac; color:#234e52; padding:12px 16px; border-radius:8px; margin-bottom:20px; font-size:14px;">
            ${sessionScope.flash}
        </div>
        <c:remove var="flash" scope="session"/>
    </c:if>

    <div class="order-detail-grid">
        <section>
            <!-- DANH SÁCH SẢN PHẨM & BẢO HÀNH -->
            <article class="detail-card">
                <h2>Danh sách sản phẩm & Thông tin bảo hành</h2>
                <c:forEach items="${orderItems}" var="item">
                    <div class="order-product" style="padding:14px 0; border-bottom:1px solid #eee;">
                        <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:12px;">
                            <div>
                                <b style="font-size:15px; color:#1a202c;">${item.productName}</b>
                                <div style="font-size:13px; color:#666; margin-top:2px;">
                                    ${not empty item.variantName ? item.variantName : 'Tiêu chuẩn'} · SKU: ${item.sku} · Số lượng: ${item.quantity}
                                </div>
                            </div>
                            <strong style="color:#d4af37; font-size:15px;"><fmt:formatNumber value="${item.lineTotal}" pattern="#,##0"/>₫</strong>
                        </div>

                        <!-- THÔNG TIN BẢO HÀNH CỦA SẢN PHẨM -->
                        <div style="margin-top:10px; background:#f8fafc; border:1px solid #e2e8f0; border-radius:6px; padding:10px 12px; font-size:13px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;">
                            <div>
                                <span>🛡 <b>Bảo hành:</b> ${item.warrantyMonths != null ? item.warrantyMonths : 12} tháng</span>
                                <span style="margin:0 6px; color:#cbd5e0;">|</span>
                                <span><b>Bắt đầu:</b> <fmt:formatDate value="${item.warrantyStartDate}" pattern="dd/MM/yyyy"/></span>
                                <span style="margin:0 6px; color:#cbd5e0;">|</span>
                                <span><b>Hết hạn:</b> <fmt:formatDate value="${item.warrantyEndDate}" pattern="dd/MM/yyyy"/></span>
                            </div>
                            <div>
                                <c:choose>
                                    <c:when test="${item.isWarrantyExpired}">
                                        <span style="background:#fee2e2; color:#991b1b; padding:3px 8px; border-radius:4px; font-weight:600; font-size:12px;">
                                            ✕ Đã hết hạn bảo hành
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="background:#d1fae5; color:#065f46; padding:3px 8px; border-radius:4px; font-weight:600; font-size:12px;">
                                            ✓ Còn bảo hành (còn ${item.remainingWarrantyDays} ngày)
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </article>

            <!-- LỊCH SỬ PHIẾU BẢO HÀNH ĐÃ GỬI CHO ĐƠN NÀY -->
            <c:if test="${not empty warranties}">
                <article class="detail-card" style="margin-top:20px;">
                    <h2>Phiếu yêu cầu bảo hành đã gửi (${fn:length(warranties)})</h2>
                    <div style="display:grid; gap:16px;">
                        <c:forEach var="w" items="${warranties}">
                            <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:16px;">
                                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                                    <b>Mã phiếu: #${w.id} · ${w.productName}</b>
                                    <span class="status-badge" style="padding:4px 10px; border-radius:4px; font-size:12px; font-weight:700;
                                        <c:choose>
                                            <c:when test="${w.status == 'Chờ tiếp nhận'}">background:#fef3c7; color:#92400e;</c:when>
                                            <c:when test="${w.status == 'Đã tiếp nhận' or w.status == 'Đang xử lý'}">background:#dbeafe; color:#1e40af;</c:when>
                                            <c:when test="${w.status == 'Hoàn tất' or w.status == 'Đã trả khách'}">background:#d1fae5; color:#065f46;</c:when>
                                            <c:otherwise>background:#fee2e2; color:#991b1b;</c:otherwise>
                                        </c:choose>
                                    ">
                                        ${w.status}
                                    </span>
                                </div>
                                <p style="margin:4px 0; color:#4a5568; font-size:13px;"><b>Lý do báo lỗi:</b> ${w.note}</p>
                                <p style="margin:4px 0; color:#718096; font-size:12px;">
                                    Hạn bảo hành: <fmt:formatDate value="${w.startDate}" pattern="dd/MM/yyyy"/> - <fmt:formatDate value="${w.endDate}" pattern="dd/MM/yyyy"/>
                                </p>

                                <!-- HIỂN THỊ ẢNH MINH CHỨNG THẬT -->
                                <c:if test="${not empty w.imageUrl}">
                                    <div style="margin-top:10px;">
                                        <b style="display:block; font-size:12px; color:#4a5568; margin-bottom:4px;">Ảnh minh chứng:</b>
                                        <c:choose>
                                            <c:when test="${fn:startsWith(w.imageUrl, 'http://') || fn:startsWith(w.imageUrl, 'https://')}">
                                                <img src="${w.imageUrl}" alt="Ảnh lỗi bảo hành" style="max-width:180px; max-height:140px; border-radius:6px; object-fit:cover; border:1px solid #cbd5e0; cursor:pointer;" onclick="window.open(this.src, '_blank')"/>
                                            </c:when>
                                            <c:when test="${fn:startsWith(w.imageUrl, '/')}">
                                                <img src="${cp}${w.imageUrl}" alt="Ảnh lỗi bảo hành" style="max-width:180px; max-height:140px; border-radius:6px; object-fit:cover; border:1px solid #cbd5e0; cursor:pointer;" onclick="window.open(this.src, '_blank')"/>
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${cp}/assets/images/${w.imageUrl}" alt="Ảnh lỗi bảo hành" style="max-width:180px; max-height:140px; border-radius:6px; object-fit:cover; border:1px solid #cbd5e0; cursor:pointer;" onerror="this.onerror=null;this.src='${cp}/assets/images/watch-1.png';" onclick="window.open(this.src, '_blank')"/>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </c:if>

                                <c:if test="${not empty w.repairContent}">
                                    <div style="margin-top:8px; padding-top:8px; border-top:1px dashed #cbd5e0; font-size:12px; color:#1e40af;">
                                        <b>Kết quả xử lý:</b> ${w.repairContent}
                                    </div>
                                </c:if>
                            </div>
                        </c:forEach>
                    </div>
                </article>
            </c:if>

            <article class="detail-card" style="margin-top:20px;">
                <h2>Thông tin nhận hàng</h2>
                <b>${order.customerName}</b>
                <p style="margin:4px 0; color:#4a5568;">Điện thoại: ${order.phone}</p>
                <p style="margin:4px 0; color:#4a5568;">Địa chỉ: ${order.shippingAddress}</p>
            </article>
        </section>

        <aside class="detail-card payment-summary">
            <h2>Thông tin thanh toán</h2>
            <p><span>Tổng tiền</span><b><fmt:formatNumber value="${order.totalPrice}" pattern="#,##0"/>₫</b></p>
            <p><span>Trạng thái thanh toán</span>
                <b>
                    <c:choose>
                        <c:when test="${order.paymentStatus == 'PAID'}">Đã thanh toán</c:when>
                        <c:when test="${order.paymentStatus == 'WAITING_PAYMENT'}">Chờ thanh toán</c:when>
                        <c:otherwise>Chưa thanh toán (COD)</c:otherwise>
                    </c:choose>
                </b>
            </p>

            <c:if test="${order.status == 'PENDING'}">
                <hr style="margin:16px 0; border:0; border-top:1px solid #e2e8f0;">
                <form action="${cp}/orders/cancel" method="post">
                    <input type="hidden" name="orderId" value="${order.id}">
                    <label style="display:block; margin-bottom:8px; font-weight:600; font-size:14px;">Lý do hủy đơn hàng:
                        <textarea name="reason" required placeholder="Vui lòng nhập lý do hủy đơn (từ 3 ký tự trở lên)..." style="width:100%; border:1px solid #cbd5e0; border-radius:6px; padding:8px; font-family:inherit; margin-top:4px; height:70px;"></textarea>
                    </label>
                    <button type="submit" class="button button-dark full" style="width:100%; padding:10px; background:#e53e3e; color:#fff; border:none; border-radius:6px; font-weight:600; cursor:pointer;" onclick="return confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')">
                        Hủy đơn hàng
                    </button>
                </form>
            </c:if>

            <c:if test="${order.status == 'COMPLETED'}">
                <hr style="margin:16px 0; border:0; border-top:1px solid #e2e8f0;">
                <h3 style="font-size:15px; font-weight:700; margin-bottom:12px; color:#1a202c;">Hỗ trợ sau bán hàng</h3>

                <div style="margin-bottom:12px;">
                    <a href="${cp}/page/reviews" class="button button-gold full" style="display:block; text-align:center; text-decoration:none; padding:10px; font-weight:600; border-radius:6px;">
                        ⭐ Đánh giá sản phẩm đã mua
                    </a>
                </div>

                <details style="background:#f8fafc; padding:12px; border-radius:8px; border:1px solid #e2e8f0;" open>
                    <summary style="cursor:pointer; font-weight:600; color:#2b6cb0;">🛡 Gửi yêu cầu bảo hành online</summary>
                    <form action="${cp}/orders/warranty" method="post" style="margin-top:12px;">
                        <input type="hidden" name="orderId" value="${order.id}">
                        <input type="hidden" name="orderCode" value="${order.code}">

                        <label style="display:block; margin-bottom:8px; font-size:13px; font-weight:600;">Chọn sản phẩm cần bảo hành:
                            <select name="productName" required style="width:100%; padding:8px; margin-top:4px; border:1px solid #cbd5e0; border-radius:6px;">
                                <c:forEach items="${orderItems}" var="item">
                                    <c:choose>
                                        <c:when test="${item.isWarrantyExpired}">
                                            <option value="${item.productName}" disabled>${item.productName} (Đã hết hạn bảo hành)</option>
                                        </c:when>
                                        <c:otherwise>
                                            <option value="${item.productName}">${item.productName} (Còn BH đến <fmt:formatDate value="${item.warrantyEndDate}" pattern="dd/MM/yyyy"/>)</option>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </select>
                        </label>

                        <label style="display:block; margin-bottom:8px; font-size:13px; font-weight:600;">Lý do bảo hành / Mô tả lỗi: <span style="color:#e53e3e;">*</span>
                            <textarea name="note" required placeholder="Mô tả chi tiết lỗi gặp phải (đồng hồ chạy chậm, vào nước, trầy xước...)" style="width:100%; padding:8px; margin-top:4px; border:1px solid #cbd5e0; border-radius:6px; font-family:inherit; height:70px;"></textarea>
                        </label>

                        <label style="display:block; margin-bottom:12px; font-size:13px; font-weight:600;">Ảnh minh chứng (Link ảnh hoặc đường dẫn file):
                            <input type="text" name="imageUrl" placeholder="Ví dụ: /assets/images/warranty-proof.jpg hoặc link ảnh..." style="width:100%; padding:8px; margin-top:4px; border:1px solid #cbd5e0; border-radius:6px; font-size:13px;"/>
                            <small style="display:block; color:#718096; margin-top:4px;">Bạn có thể cung cấp đường dẫn ảnh chụp chi tiết vị trí bị lỗi.</small>
                        </label>

                        <button type="submit" style="width:100%; padding:10px; background:#2b6cb0; color:#fff; border:none; border-radius:6px; font-weight:600; cursor:pointer;">
                            Gửi yêu cầu bảo hành
                        </button>
                    </form>
                </details>
            </c:if>
        </aside>
    </div>
</main>