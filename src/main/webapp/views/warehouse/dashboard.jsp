<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>

        <%-- dashboard.jsp — Tổng quan kho hàng --%>

            <div class="portal-welcome">
                <div>
                    <p class="eyebrow dark">TRUNG TÂM KHO HÀNG</p>
                    <h2>Xin chào, ${empty sessionScope.user ? 'Nhân viên kho' : sessionScope.user.fullName}</h2>
                </div>
            </div>

            <c:if test="${not empty sessionScope.flashMessage}">
                <div class="flash-message">${sessionScope.flashMessage}</div>
                <c:remove var="flashMessage" scope="session" />
            </c:if>

            <div class="metric-grid">

                <article>
                    <span>Tổng biến thể sản phẩm</span>
                    <b>${totalVariants}</b>
                </article>

                <article>
                    <span>Tổng tồn kho (sản phẩm)</span>
                    <b>${totalInventory}</b>
                </article>

                <article>
                    <span>Phiếu nhập hôm nay</span>
                    <b>${todayReceipts}</b>
                </article>

                <article>
                    <span>Phiếu xuất hôm nay</span>
                    <b>${todayExports}</b>
                </article>

            </div>

            <c:if test="${lowStockCount > 0}">
                <div class="alert-banner">
                    <span>⚠ Có <b>${lowStockCount}</b> biến thể đang dưới ngưỡng tồn kho tối thiểu.</span>
                    <a href="${pageContext.request.contextPath}/manage/warehouse/alerts">Xem cảnh báo →</a>
                </div>
            </c:if>

            <div class="quick-links">
                <a href="${pageContext.request.contextPath}/manage/warehouse/receipts">⇩ Phiếu nhập kho</a>
                <a href="${pageContext.request.contextPath}/manage/warehouse/exports">⇧ Phiếu xuất kho</a>
                <a href="${pageContext.request.contextPath}/manage/warehouse/inventory">▣ Xem tồn kho</a>
                <a href="${pageContext.request.contextPath}/manage/warehouse/stocktake">✓ Kiểm kê kho</a>
            </div>