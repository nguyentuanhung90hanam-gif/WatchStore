<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">CẢNH BÁO THÔNG MINH</p>
        <h2>Cảnh báo tồn kho</h2>
        <p class="module-desc">Phát hiện sản phẩm sắp hết hoặc đã hết theo ngưỡng tồn kho tối thiểu.</p>
    </div>
    <a class="button button-gold" href="${cp}/manage/warehouse/receipt-create">+ Tạo phiếu nhập nhanh</a>
</div>

<div class="dashboard-card">
    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Kho</th>
                    <th>Sản phẩm</th>
                    <th>SKU</th>
                    <th>Tồn khả dụng</th>
                    <th>Mức an toàn</th>
                    <th>Trạng thái cảnh báo</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${lowStockItems}" var="item">
                    <tr>
                        <td>${item.warehouseName}</td>
                        <td><b>${item.productName}</b><br><small>${item.variantName}</small></td>
                        <td>${item.sku}</td>
                        <td style="color: red; font-weight: bold;">${item.availableQuantity}</td>
                        <td>${item.reorderLevel}</td>
                        <td>
                            <span class="status-badge warning">${item.stockStatus}</span>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty lowStockItems}">
                    <tr>
                        <td colspan="6" style="text-align: center;">Mọi mặt hàng đều ở mức an toàn.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>
