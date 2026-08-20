<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<main class="page-shell account-page">
    <jsp:include page="/views/shared/account-nav.jsp" />
    <section class="account-content">
        <div class="account-heading">
            <div>
                <p class="eyebrow dark">CHIA SẺ TRẢI NGHIỆM</p>
                <h1>Đánh giá sản phẩm</h1>
            </div>
        </div>

        <c:if test="${not empty sessionScope.flash}">
            <div style="background:#e6fffa; border:1px solid #38b2ac; color:#234e52; padding:12px 16px; border-radius:8px; margin-bottom:20px; font-size:14px;">
                ${sessionScope.flash}
            </div>
            <c:remove var="flash" scope="session"/>
        </c:if>

        <!-- SẢN PHẨM CHỜ ĐÁNH GIÁ -->
        <div style="margin-bottom:36px;">
            <h2 style="font-size:18px; font-weight:700; color:#1a202c; margin-bottom:16px;">
                Sản phẩm chờ đánh giá (${fn:length(pendingReviewItems)})
            </h2>

            <c:choose>
                <c:when test="${empty pendingReviewItems}">
                    <div style="background:#f8fafc; border:1px dashed #cbd5e0; border-radius:10px; padding:24px; text-align:center;">
                        <p style="color:#718096; margin:0;">Bạn không có sản phẩm nào từ đơn hàng đã hoàn thành đang chờ đánh giá.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="display:grid; gap:20px;">
                        <c:forEach var="item" items="${pendingReviewItems}">
                            <article class="review-form-card" style="border:1px solid #e2e8f0; border-radius:10px; padding:20px; background:#fff; display:flex; gap:20px; flex-wrap:wrap;">
                                <div style="width:100px; height:100px; flex-shrink:0; background:#edf2f7; border-radius:8px; overflow:hidden; display:flex; align-items:center; justify-content:center;">
                                    <c:choose>
                                        <c:when test="${not empty item.productImage}">
                                            <img src="${item.productImage}" alt="${item.productName}" style="max-width:100%; max-height:100%; object-fit:cover;"
                                                 onerror="this.onerror=null;this.src='${cp}/assets/images/watch-1.png';">
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${cp}/assets/images/watch-1.png" alt="${item.productName}" style="max-width:100%; max-height:100%; object-fit:cover;">
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div style="flex:1; min-width:260px;">
                                    <small style="color:#718096; display:block;">Đơn hàng #${item.orderCode}</small>
                                    <h3 style="margin:4px 0 12px 0; font-size:16px; color:#1a202c;">${item.productName}</h3>

                                    <form action="${cp}/reviews/add" method="post">
                                        <input type="hidden" name="productId" value="${item.productId}">
                                        <input type="hidden" name="orderId" value="${item.orderId}">

                                        <div style="margin-bottom:12px;">
                                            <label style="display:block; font-weight:600; font-size:13px; margin-bottom:4px;">Chất lượng sản phẩm:</label>
                                            <select name="rating" required style="padding:6px 12px; border:1px solid #cbd5e0; border-radius:6px; font-weight:600; color:#d4af37;">
                                                <option value="5">★★★★★ (5 sao - Rất tốt)</option>
                                                <option value="4">★★★★☆ (4 sao - Tốt)</option>
                                                <option value="3">★★★☆☆ (3 sao - Bình thường)</option>
                                                <option value="2">★★☆☆☆ (2 sao - Tạm được)</option>
                                                <option value="1">★☆☆☆☆ (1 sao - Kém)</option>
                                            </select>
                                        </div>

                                        <div style="margin-bottom:12px;">
                                            <label style="display:block; font-weight:600; font-size:13px; margin-bottom:4px;">Tiêu đề đánh giá (tùy chọn):</label>
                                            <input type="text" name="title" placeholder="Tóm tắt cảm nhận của bạn (ví dụ: Đồng hồ rất đẹp, chạy chuẩn...)"
                                                   style="width:100%; padding:8px 12px; border:1px solid #cbd5e0; border-radius:6px; font-size:14px;">
                                        </div>

                                        <div style="margin-bottom:12px;">
                                            <label style="display:block; font-weight:600; font-size:13px; margin-bottom:4px;">Nội dung đánh giá:</label>
                                            <textarea name="content" required rows="3" placeholder="Sản phẩm có đúng như mô tả? Đóng gói thế nào? Cảm nhận khi đeo..."
                                                      style="width:100%; padding:8px 12px; border:1px solid #cbd5e0; border-radius:6px; font-size:14px; font-family:inherit;"></textarea>
                                        </div>

                                        <button type="submit" class="button button-gold" style="padding:8px 20px; font-size:14px; font-weight:600;">
                                            Gửi đánh giá
                                        </button>
                                        <p class="form-note" style="color:#a0aec0; font-size:12px; margin-top:8px;">
                                            Đánh giá của bạn sẽ giúp người mua khác có cái nhìn chân thực về sản phẩm.
                                        </p>
                                    </form>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- LỊCH SỬ ĐÁNH GIÁ ĐÃ GỬI -->
        <div>
            <h2 style="font-size:18px; font-weight:700; color:#1a202c; margin-bottom:16px;">
                Đánh giá đã gửi (${fn:length(myReviews)})
            </h2>

            <c:choose>
                <c:when test="${empty myReviews}">
                    <div style="background:#f8fafc; border:1px dashed #cbd5e0; border-radius:10px; padding:24px; text-align:center;">
                        <p style="color:#718096; margin:0;">Bạn chưa gửi đánh giá nào.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="display:grid; gap:16px;">
                        <c:forEach var="rev" items="${myReviews}">
                            <div style="border:1px solid #e2e8f0; border-radius:10px; padding:16px; background:#fff;">
                                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                                    <div>
                                        <b style="color:#1a202c; font-size:15px;">${rev.productName}</b>
                                        <c:if test="${not empty rev.orderCode}">
                                            <span style="color:#718096; font-size:12px; margin-left:8px;">(Đơn hàng #${rev.orderCode})</span>
                                        </c:if>
                                    </div>
                                    <span style="color:#a0aec0; font-size:12px;">
                                        <fmt:formatDate value="${rev.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                    </span>
                                </div>
                                <div style="color:#d4af37; font-size:15px; margin-bottom:6px;">
                                    <c:forEach begin="1" end="5" var="s">
                                        <c:choose>
                                            <c:when test="${s <= rev.rating}">★</c:when>
                                            <c:otherwise>☆</c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                    <span style="color:#718096; font-size:13px; margin-left:6px;">(${rev.rating}/5 sao)</span>
                                </div>
                                <c:if test="${not empty rev.title}">
                                    <b style="display:block; color:#2d3748; margin-bottom:4px; font-size:14px;">${rev.title}</b>
                                </c:if>
                                <p style="color:#4a5568; margin:0; font-size:14px; line-height:1.5;">${rev.content}</p>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>
