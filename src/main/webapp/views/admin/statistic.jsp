<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">${moduleKicker}</p>
        <h2>${moduleTitle}</h2>
        <p class="module-desc">${moduleDescription}</p>
    </div>
</div>

<div class="metric-grid" style="margin-bottom:24px;">
    <article>
        <span>Tổng doanh thu (Thật DB)</span>
        <b><fmt:formatNumber value="${totalRevenue}" pattern="#,##0"/>₫</b>
        <em>Tối ưu từ Orders</em>
    </article>
    <article>
        <span>Tổng đơn hàng</span>
        <b>${totalOrdersCount}</b>
        <em>Đơn hàng DB</em>
    </article>
    <article>
        <span>Tổng sản phẩm</span>
        <b>${totalProductsCount}</b>
        <em>Sản phẩm DB</em>
    </article>
    <article>
        <span>Tổng tài khoản</span>
        <b>${totalCustomersCount}</b>
        <em>Người dùng DB</em>
    </article>
</div>

<div class="dashboard-grid" style="margin-bottom:24px;">

    <%-- Thống kê theo trạng thái đơn hàng --%>
    <article class="dashboard-card">
        <div class="card-title">
            <div><b>Đơn hàng theo trạng thái</b><span>Dữ liệu thực tế từ bảng Orders</span></div>
        </div>
        <div class="table-wrap" style="padding:16px;">
            <table>
                <thead>
                <tr>
                    <th>Trạng thái</th>
                    <th>Số lượng đơn</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${orderStatusCounts}" var="entry">
                    <tr>
                        <td><span class="status-badge success">${entry.key}</span></td>
                        <td><b>${entry.value} đơn</b></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty orderStatusCounts}">
                    <tr>
                        <td colspan="2" style="text-align:center;color:#888;padding:16px;">
                            Chưa có đơn hàng nào trong database.
                        </td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </article>

    <%-- Thống kê sản phẩm theo danh mục --%>
    <article class="dashboard-card">
        <div class="card-title">
            <div><b>Sản phẩm theo danh mục</b><span>Dữ liệu thực tế từ bảng Categories & Products</span></div>
        </div>
        <div class="table-wrap" style="padding:16px;">
            <table>
                <thead>
                <tr>
                    <th>Tên danh mục</th>
                    <th>Số sản phẩm</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${categoryCounts}" var="entry">
                    <tr>
                        <td><b>${entry.key}</b></td>
                        <td>${entry.value} sản phẩm</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty categoryCounts}">
                    <tr>
                        <td colspan="2" style="text-align:center;color:#888;padding:16px;">
                            Chưa có danh mục sản phẩm nào trong database.
                        </td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </article>

</div>

<div class="dashboard-grid" style="margin-bottom:24px;">

    <%-- Thống kê sản phẩm theo thương hiệu --%>
    <article class="dashboard-card">
        <div class="card-title">
            <div><b>Sản phẩm theo thương hiệu</b><span>Dữ liệu thực tế từ bảng Brands & Products</span></div>
        </div>
        <div class="table-wrap" style="padding:16px;">
            <table>
                <thead>
                <tr>
                    <th>Thương hiệu</th>
                    <th>Số sản phẩm</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${brandCounts}" var="entry">
                    <tr>
                        <td><b>${entry.key}</b></td>
                        <td>${entry.value} sản phẩm</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty brandCounts}">
                    <tr>
                        <td colspan="2" style="text-align:center;color:#888;padding:16px;">
                            Chưa có thương hiệu nào trong database.
                        </td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </article>

    <%-- Thống kê tổng hợp khác --%>
    <article class="dashboard-card">
        <div class="card-title">
            <div><b>Chỉ số khác trong hệ thống</b><span>Thông số tổng quan DB</span></div>
        </div>
        <div style="padding:20px;display:flex;flex-direction:column;gap:12px;">
            <div style="display:flex;justify-content:space-between;border-bottom:1px solid #eee;padding-bottom:8px;">
                <span>Số thương hiệu:</span>
                <b>${totalBrandsCount}</b>
            </div>
            <div style="display:flex;justify-content:space-between;border-bottom:1px solid #eee;padding-bottom:8px;">
                <span>Số danh mục:</span>
                <b>${totalCategoriesCount}</b>
            </div>
            <div style="display:flex;justify-content:space-between;border-bottom:1px solid #eee;padding-bottom:8px;">
                <span>Mã Voucher khởi tạo:</span>
                <b>${totalVouchersCount}</b>
            </div>
            <div style="display:flex;justify-content:space-between;padding-bottom:8px;">
                <span>Sản phẩm cảnh báo tồn thấp:</span>
                <b style="color:${lowStockCount > 0 ? '#d9534f' : '#28a745'};">${lowStockCount}</b>
            </div>
        </div>
    </article>

</div>
