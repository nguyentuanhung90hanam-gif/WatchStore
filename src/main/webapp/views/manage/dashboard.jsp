<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />

<div class="portal-welcome">
    <div>
        <p class="eyebrow dark">TỔNG QUAN QUẢN LÝ</p>
        <h2>Xin chào, ${empty sessionScope.user.fullName ? 'Quản trị viên' : sessionScope.user.fullName}</h2>
        <span>Hệ thống quản lý dữ liệu đồng hồ chính hãng WatchStore.</span>
    </div>
    <div class="date-chip">
        TRANG TỔNG QUAN REAL-TIME
    </div>
</div>

<div class="admin-metric-grid">
    <!-- BÁN HÀNG -->
    <c:if test="${sessionScope.user.hasAnyPermission('ORDERS_VIEW', 'ORDERS_MANAGE', 'CUSTOMERS_VIEW')}">
        <article class="stat-card">
            <div class="stat-card-header">
                <span class="stat-title">ĐƠN HÀNG HÔM NAY</span>
            </div>
            <div class="stat-value"><fmt:formatNumber value="${empty todayOrders ? 0 : todayOrders}" pattern="#,##0" /></div>
            <div class="stat-desc">
                <c:choose>
                    <c:when test="${pendingOrders > 0}">
                        <span class="stat-badge warning">⚠ ${pendingOrders} đơn chờ xử lý</span>
                    </c:when>
                    <c:otherwise>
                        <span class="stat-badge success">✓ Đã xử lý tất cả</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </article>

        <article class="stat-card">
            <div class="stat-card-header">
                <span class="stat-title">DOANH THU HÔM NAY</span>
            </div>
            <div class="stat-value gold-text">
                <fmt:formatNumber value="${empty todayRevenue ? 0 : todayRevenue}" pattern="#,##0" /> ₫
            </div>
            <div class="stat-desc">
                <span class="stat-badge info">Tổng doanh thu thực tế</span>
            </div>
        </article>

        <article class="stat-card">
            <div class="stat-card-header">
                <span class="stat-title">KHÁCH HÀNG</span>
            </div>
            <div class="stat-value"><fmt:formatNumber value="${empty totalCustomers ? 0 : totalCustomers}" pattern="#,##0" /></div>
            <div class="stat-desc">
                <span class="stat-badge success">Hôm nay: +${empty newCustomersToday ? 0 : newCustomersToday} khách mới</span>
            </div>
        </article>
    </c:if>

    <!-- KHO HÀNG -->
    <c:if test="${sessionScope.user.hasAnyPermission('INVENTORY_VIEW', 'INVENTORY_MANAGE')}">
        <article class="stat-card">
            <div class="stat-card-header">
                <span class="stat-title">TỒN KHO</span>
            </div>
            <div class="stat-value"><fmt:formatNumber value="${empty totalInventory ? 0 : totalInventory}" pattern="#,##0" /></div>
            <div class="stat-desc">
                <span class="stat-badge ${outOfStockCount > 0 ? 'danger' : 'neutral'}">${outOfStockCount} sản phẩm hết hàng</span>
            </div>
        </article>

        <article class="stat-card">
            <div class="stat-card-header">
                <span class="stat-title">CẢNH BÁO TỒN KHO</span>
            </div>
            <div class="stat-value"><fmt:formatNumber value="${empty lowStockAlerts ? 0 : lowStockAlerts}" pattern="#,##0" /></div>
            <div class="stat-desc">
                <span class="stat-badge warning">Sản phẩm sắp hết</span>
            </div>
        </article>
    </c:if>

    <!-- ĐỔI TRẢ VÀ BẢO HÀNH -->
    <c:if test="${sessionScope.user.hasAnyPermission('RETURNS_VIEW', 'WARRANTY_VIEW')}">
        <article class="stat-card">
            <div class="stat-card-header">
                <span class="stat-title">ĐỔI TRẢ & BẢO HÀNH</span>
            </div>
            <div class="stat-value">${empty activeWarranties ? 0 : activeWarranties} <small style="font-size: 12px; color: #888; font-weight: normal;">Bảo hành</small></div>
            <div class="stat-desc">
                <span class="stat-badge neutral">${empty totalReturns ? 0 : totalReturns} yêu cầu đổi trả</span>
            </div>
        </article>
    </c:if>

    <!-- PHIẾU KHO -->
    <c:if test="${sessionScope.user.hasAnyPermission('STOCK_RECEIPT_VIEW', 'STOCK_EXPORT_VIEW')}">
        <article class="stat-card">
            <div class="stat-card-header">
                <span class="stat-title">PHIẾU KHO</span>
            </div>
            <div class="stat-value">${empty stockReceipts ? 0 : stockReceipts} <small style="font-size: 12px; color: #888; font-weight: normal;">Nhập</small></div>
            <div class="stat-desc">
                <span class="stat-badge info">${empty stockExports ? 0 : stockExports} Xuất / ${empty stocktakes ? 0 : stocktakes} Kiểm kê</span>
            </div>
        </article>
    </c:if>
</div>

<c:if test="${sessionScope.user.hasAnyPermission('PRODUCTS_VIEW', 'ORDERS_VIEW', 'DASHBOARD_VIEW')}">
    <div class="dashboard-grid">
        <article class="dashboard-card latest-table">
            <div class="card-title">
                <div>
                    <b>Sản phẩm bán chạy</b>
                    <span>Dữ liệu bán hàng cập nhật realtime</span>
                </div>
            </div>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>SẢN PHẨM</th>
                            <th>MÃ SKU</th>
                            <th>ĐÃ BÁN</th>
                            <th>DOANH THU</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${topProducts}" var="prod">
                            <tr>
                                <td><b>${prod.productName}</b><br><small>${prod.variantName}</small></td>
                                <td>${prod.sku}</td>
                                <td>${prod.quantitySold}</td>
                                <td class="amount-cell"><fmt:formatNumber value="${prod.revenue}" pattern="#,##0" /> ₫</td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty topProducts}">
                            <tr>
                                <td colspan="4" class="empty-table-msg">Chưa có dữ liệu sản phẩm bán chạy.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </article>
    </div>
</c:if>
