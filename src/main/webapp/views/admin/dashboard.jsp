<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />

<div class="portal-welcome">
    <div>
        <p class="eyebrow dark">ĐIỀU HÀNH HỆ THỐNG</p>
        <h2>Xin chào, ${empty sessionScope.user.fullName ? 'Quản trị viên' : sessionScope.user.fullName}</h2>
        <span>Hệ thống quản lý dữ liệu đồng hồ chính hãng WatchStore.</span>
    </div>
    <div class="date-chip">TRANG TỔNG QUAN REAL-TIME</div>
</div>

<!-- 1. STATISTIC CARDS -->
<div class="admin-metric-grid">

    <article class="stat-card">
        <div class="stat-card-header">
            <div class="stat-icon icon-gold">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="9" cy="21" r="1"></circle>
                    <circle cx="20" cy="21" r="1"></circle>
                    <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                </svg>
            </div>
            <span class="stat-title">TỔNG ĐƠN HÀNG</span>
        </div>

        <div class="stat-value">
            <fmt:formatNumber value="${totalOrders}" pattern="#,##0" />
        </div>

        <div class="stat-desc">
            <c:choose>
                <c:when test="${pendingOrders > 0}">
                    <span class="stat-badge warning"> ${pendingOrders} đơn chờ xác nhận</span>
                </c:when>
                <c:otherwise>
                    <span class="stat-badge success">✓ Tất cả đơn đã xử lý</span>
                </c:otherwise>
            </c:choose>
        </div>
    </article>

    <article class="stat-card">
        <div class="stat-card-header">
            <div class="stat-icon icon-dark">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="7"></circle>
                    <polyline points="12 9 12 12 13.5 13.5"></polyline>
                    <path d="M16.51 17.35l-.85 3.19a1 1 0 0 1-1.22.71l-4.88-1.3a1 1 0 0 1-.71-1.22l.85-3.19"></path>
                    <path d="M7.49 6.65l.85-3.19a1 1 0 0 1 1.22-.71l4.88 1.3a1 1 0 0 1 .71 1.22l-.85 3.19"></path>
                </svg>
            </div>
            <span class="stat-title">SẢN PHẨM</span>
        </div>

        <div class="stat-value">
            <fmt:formatNumber value="${empty totalProducts ? 0 : totalProducts}" pattern="#,##0" />
        </div>

        <div class="stat-desc">
            <span class="stat-badge neutral">
                Active: ${empty activeProducts ? 0 : activeProducts} đang kinh doanh
            </span>
        </div>
    </article>

    <article class="stat-card">
        <div class="stat-card-header">
            <div class="stat-icon icon-blue">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                    <circle cx="9" cy="7" r="4"></circle>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
            </div>
            <span class="stat-title">KHÁCH HÀNG</span>
        </div>

        <div class="stat-value">
            <fmt:formatNumber value="${empty totalUsers ? 0 : totalUsers}" pattern="#,##0" />
        </div>

        <div class="stat-desc">
            <span class="stat-badge success">
                Hoạt động: ${empty activeUsers ? 0 : activeUsers} tài khoản
            </span>
        </div>
    </article>

</div>

<!-- 2. BIỂU ĐỒ DOANH THU 7 NGÀY -->
<article class="dashboard-card chart-card">
    <div class="card-title">
        <div>
            <b>Doanh thu 7 ngày gần nhất</b>
            <span>Dữ liệu biến động doanh thu thực tế từ đơn hàng</span>
        </div>
        <span class="chart-tag">Realtime DB</span>
    </div>

    <div class="revenue-chart-container">
        <div class="revenue-bars">

            <c:forEach items="${last7DaysSales}" var="day">
                <div class="bar-column">

                    <div class="bar-tooltip">
                        <b>
                            <c:choose>
                                <c:when test="${not empty day.revenue and day.revenue > 0}">
                                    <fmt:formatNumber value="${day.revenue}" pattern="#,##0" /> ₫
                                </c:when>
                                <c:otherwise>0 ₫</c:otherwise>
                            </c:choose>
                        </b>
                        <small>${day.ordersCount} đơn hàng</small>
                    </div>

                    <div class="bar-track">
                        <div class="bar-fill ${day.isMax ? 'gold' : 'dark'}"
                             style="height:${day.heightPercent}%;"></div>
                    </div>

                    <span class="bar-date-label">${day.dateLabel}</span>
                </div>
            </c:forEach>

        </div>
    </div>
</article>

<!-- 3. BẢNG DỮ LIỆU VẬN HÀNH GẦN NHẤT -->
<article class="dashboard-card latest-table">
    <div class="card-title">
        <div>
            <b>Dữ liệu vận hành gần nhất</b>
            <span>Danh sách các đơn hàng mới nhất từ database</span>
        </div>
    </div>

    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>MÃ</th>
                    <th>THÔNG TIN</th>
                    <th>THỜI GIAN</th>
                    <th>GIÁ TRỊ</th>
                    <th>TRẠNG THÁI</th>
                </tr>
            </thead>

            <tbody>

                <c:forEach items="${orders}" var="order">
                    <tr>
                        <td>
                            <b class="order-code">#${order.code}</b>
                        </td>

                        <td>
                            <div class="customer-info-cell">
                                <span class="customer-name">${order.customerName}</span>
                            </div>
                        </td>

                        <td class="time-cell">
                            ${order.createdAt}
                        </td>

                        <td class="amount-cell">
                            <fmt:formatNumber value="${order.total}" pattern="#,##0" /> ₫
                        </td>

                        <td>
                            <span class="status-pill status-${order.status.cssClass}">
                                ${order.status.label}
                            </span>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty orders}">
                    <tr>
                        <td colspan="5" class="empty-table-msg">
                            Chưa có đơn hàng nào trong hệ thống.
                        </td>
                    </tr>
                </c:if>

            </tbody>
        </table>
    </div>
</article>