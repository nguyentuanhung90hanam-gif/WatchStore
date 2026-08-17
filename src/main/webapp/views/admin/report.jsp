<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="module-heading" style="display:flex;justify-content:space-between;align-items:flex-start;">
    <div class="module-title-area">
        <p class="eyebrow dark">${moduleKicker}</p>
        <h2>${moduleTitle}</h2>
        <p class="module-desc">${moduleDescription}</p>
    </div>
    <div style="margin-top:8px;">
        <a href="${pageContext.request.contextPath}/manage/admin/reports/pdf" target="_blank" class="btn" style="display:inline-flex;align-items:center;gap:8px;padding:10px 18px;background:var(--gold-dark,#b8860b);color:#fff;border-radius:6px;text-decoration:none;font-weight:600;font-size:0.9em;box-shadow:0 2px 6px rgba(0,0,0,0.15);">
            📄 Xuất PDF
        </a>
    </div>
</div>

<div class="metric-grid" style="margin-bottom:24px;">
    <article>
        <span>Doanh thu tính báo cáo</span>
        <b><fmt:formatNumber value="${totalRevenue}" pattern="#,##0"/>₫</b>
    </article>
    <article>
        <span>Số đơn hàng toàn hệ thống</span>
        <b>${totalOrdersCount}</b>
    </article>
    <article>
        <span>Số mặt hàng quản lý</span>
        <b>${totalProductsCount}</b>
    </article>
</div>

<%-- Báo cáo sản phẩm bán chạy --%>
<div class="dashboard-card" style="margin-bottom:24px;">
    <div class="card-title">
        <div><b>Báo cáo sản phẩm bán chạy nhất</b><span>Truy vấn thực tế từ view vw_TopSellingProducts / OrderItems</span></div>
    </div>
    <div class="table-wrap">
        <table>
            <thead>
            <tr>
                <th>STT</th>
                <th>Sản phẩm</th>
                <th>SKU</th>
                <th>Số lượng đã bán</th>
                <th>Doanh thu phát sinh</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${topSellingProducts}" var="p" varStatus="st">
                <tr>
                    <td>${st.index + 1}</td>
                    <td><b>${p.productName}</b> <c:if test="${not empty p.variantName}"><small style="color:#777;">(${p.variantName})</small></c:if></td>
                    <td><b style="font-family:monospace;color:var(--gold-dark,#b8860b);">${p.sku}</b></td>
                    <td><b>${p.quantitySold}</b> sản phẩm</td>
                    <td><b><fmt:formatNumber value="${p.revenue}" pattern="#,##0"/>₫</b></td>
                </tr>
            </c:forEach>
            <c:if test="${empty topSellingProducts}">
                <tr>
                    <td colspan="5" style="text-align:center;color:#888;padding:20px;">
                        Chưa phát sinh doanh số bán hàng trong database.
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

<%-- Báo cáo khách hàng tiêu biểu --%>
<div class="dashboard-card" style="margin-bottom:24px;">
    <div class="card-title">
        <div><b>Báo cáo khách hàng tiêu biểu</b><span>Truy vấn thực tế từ view vw_CustomerSummary / Users</span></div>
    </div>
    <div class="table-wrap">
        <table>
            <thead>
            <tr>
                <th>STT</th>
                <th>Họ tên</th>
                <th>Email</th>
                <th>Số điện thoại</th>
                <th>Tổng số đơn</th>
                <th>Tổng chi tiêu</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${topCustomers}" var="c" varStatus="st">
                <tr>
                    <td>${st.index + 1}</td>
                    <td><b>${c.fullName}</b></td>
                    <td>${c.email}</td>
                    <td>${not empty c.phone ? c.phone : '—'}</td>
                    <td><b>${c.ordersCount}</b> đơn</td>
                    <td><b><fmt:formatNumber value="${c.spentAmount}" pattern="#,##0"/>₫</b></td>
                </tr>
            </c:forEach>
            <c:if test="${empty topCustomers}">
                <tr>
                    <td colspan="6" style="text-align:center;color:#888;padding:20px;">
                        Chưa có dữ liệu khách hàng trong database.
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>
