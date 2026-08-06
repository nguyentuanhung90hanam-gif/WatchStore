<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- statistics.jsp — Xem thống kê kho (Báo cáo nhập, xuất, tồn kho) --%>

<div class="module-heading">
    <div>
        <p class="eyebrow dark">THỐNG KÊ</p>
        <h2>Báo cáo kho hàng</h2>
        <p>Tổng quan hoạt động nhập xuất kho trong tháng hiện tại.</p>
    </div>
</div>

<%-- Các thẻ số liệu nhanh --%>
<div class="dashboard-grid">

    <div class="dashboard-card stat-card">
        <p class="stat-label">📦 Phiếu nhập tháng này</p>
        <p class="stat-value">${receiptsThisMonth}</p>
    </div>

    <div class="dashboard-card stat-card">
        <p class="stat-label">🚚 Phiếu xuất tháng này</p>
        <p class="stat-value">${exportsThisMonth}</p>
    </div>

    <div class="dashboard-card stat-card">
        <p class="stat-label">⚠️ Sắp hết hàng</p>
        <p class="stat-value" style="color:var(--color-warning, #f9a825);">${lowStockCount}</p>
    </div>

    <div class="dashboard-card stat-card">
        <p class="stat-label">🚫 Hết hàng hoàn toàn</p>
        <p class="stat-value" style="color:var(--color-danger, #e53935);">${outOfStockCount}</p>
    </div>

</div>

<%-- Bảng phiếu nhập gần đây --%>
<div class="dashboard-card" style="margin-top:1.5rem;">
    <h3 style="margin-bottom:1rem;">📥 Phiếu nhập gần đây</h3>
    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Mã phiếu</th>
                    <th>Nhà cung cấp</th>
                    <th>Ngày nhập</th>
                    <th>Tổng tiền</th>
                    <th>Trạng thái</th>
                    <th style="text-align:right;">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty recentReceipts}">
                        <tr><td colspan="6" style="text-align:center;">Không có dữ liệu.</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${recentReceipts}" var="r">
                            <tr>
                                <td><b>${r.receiptCode}</b></td>
                                <td>${r.supplierName}</td>
                                <td>${r.receiptDate}</td>
                                <td>
                                    <fmt:formatNumber value="${r.totalCost}" type="number" maxFractionDigits="0"/> đ
                                </td>
                                <td>
                                    <span class="status-badge ${r.statusClass}">${r.statusLabel}</span>
                                </td>
                                <td style="text-align:right;">
                                    <%-- Nút in phiếu nhập --%>
                                    <a class="table-action"
                                       href="${pageContext.request.contextPath}/manage/warehouse/receipt-print?id=${r.receiptID}"
                                       target="_blank">🖨️ In</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
    <div style="margin-top:0.75rem;">
        <a href="${pageContext.request.contextPath}/manage/warehouse/receipts" class="button button-outline">
            Xem tất cả phiếu nhập →
        </a>
    </div>
</div>

<%-- Bảng phiếu xuất gần đây --%>
<div class="dashboard-card" style="margin-top:1.5rem;">
    <h3 style="margin-bottom:1rem;">📤 Phiếu xuất gần đây</h3>
    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Mã phiếu</th>
                    <th>Loại xuất</th>
                    <th>Người nhận</th>
                    <th>Ngày xuất</th>
                    <th>Trạng thái</th>
                    <th style="text-align:right;">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty recentExports}">
                        <tr><td colspan="6" style="text-align:center;">Không có dữ liệu.</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${recentExports}" var="e">
                            <tr>
                                <td><b>${e.exportCode}</b></td>
                                <td>${e.exportType}</td>
                                <td>${e.receiverName}</td>
                                <td>${e.exportDate}</td>
                                <td>
                                    <span class="status-badge ${e.statusClass}">${e.statusLabel}</span>
                                </td>
                                <td style="text-align:right;">
                                    <a class="table-action"
                                       href="${pageContext.request.contextPath}/manage/warehouse/export-print?id=${e.exportID}"
                                       target="_blank">🖨️ In</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
    <div style="margin-top:0.75rem;">
        <a href="${pageContext.request.contextPath}/manage/warehouse/exports" class="button button-outline">
            Xem tất cả phiếu xuất →
        </a>
    </div>
</div>
