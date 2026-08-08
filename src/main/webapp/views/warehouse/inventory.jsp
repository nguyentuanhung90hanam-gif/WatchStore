<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">TỒN KHO</p>
        <h2>Quản lý Tồn kho</h2>
        <p class="module-desc">Theo dõi số lượng hàng hóa và trạng thái tồn kho thực tế.</p>
    </div>
</div>

<div class="dashboard-card">
    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Kho</th>
                    <th>SKU</th>
                    <th>Sản phẩm / Biến thể</th>
                    <th style="text-align:right;">Tồn thực tế</th>
                    <th style="text-align:right;">Đã đặt trước</th>
                    <th style="text-align:right;">Tồn khả dụng</th>
                    <th style="text-align:center;">Trạng thái</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${inventoryItems}" var="i">
                    <tr>
                        <td>${i.warehouseName}</td>
                        <td><b>${i.sku}</b></td>
                        <td>
                            <b>${i.productName}</b><br>
                            <small>${i.variantName}</small>
                        </td>
                        <td style="text-align:right;">${i.quantityOnHand}</td>
                        <td style="text-align:right;">${i.quantityReserved}</td>
                        <td style="text-align:right; font-weight:bold; color: ${i.availableQuantity <= 0 ? 'red' : 'green'}">${i.availableQuantity}</td>
                        <td style="text-align:center;">
                            <span class="status-badge
                                ${i.stockStatus == 'AN TOAN' ? 'success' :
                                  i.stockStatus == 'HẾT HÀNG' || i.stockStatus == 'H?T HANG' ? 'danger' : 'warning'}">
                                ${i.stockStatus}
                            </span>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty inventoryItems}">
                    <tr><td colspan="7" style="text-align:center;">Chưa có dữ liệu tồn kho</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<style>
.status-badge { padding: 4px 8px; border-radius: 4px; font-size: 11px; font-weight: bold; }
.status-badge.success { background:#d1fae5; color:#065f46; }
.status-badge.warning { background:#fef3c7; color:#b45309; }
.status-badge.danger { background:#fee2e2; color:#991b1b; }
</style>
