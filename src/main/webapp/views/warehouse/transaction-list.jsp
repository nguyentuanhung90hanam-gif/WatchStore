<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">LỊCH SỬ</p>
        <h2>Lịch sử biến động tồn kho</h2>
        <p class="module-desc">Theo dõi mọi giao dịch nhập, xuất và điều chỉnh kho.</p>
    </div>
</div>

<div class="dashboard-card">
    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Thời gian</th>
                    <th>Kho</th>
                    <th>Giao dịch</th>
                    <th>Sản phẩm / Biến thể</th>
                    <th style="text-align:right;">Tồn trước</th>
                    <th style="text-align:right;">Thay đổi</th>
                    <th style="text-align:right;">Tồn sau</th>
                    <th>Người thực hiện</th>
                    <th>Ghi chú</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${transactions}" var="t">
                    <tr>
                        <td style="white-space: nowrap;">
                            <fmt:parseDate value="${t.createdAt}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="parsedDate" type="both" />
                            <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedDate}" />
                        </td>
                        <td>${t.warehouseName}</td>
                        <td>
                            <span class="status-badge
                                ${t.transactionType == 'RECEIPT' || t.transactionType == 'ADJUST_IN' || t.transactionType == 'TRANSFER_IN' || t.transactionType == 'RETURN_IN' ? 'success' : 'danger'}">
                                ${t.transactionType}
                            </span>
                        </td>
                        <td>
                            <b>${t.sku}</b><br>
                            <small>${t.productName} (${t.variantName})</small>
                        </td>
                        <td style="text-align:right;">${t.quantityBefore}</td>
                        <td style="text-align:right; font-weight:bold; color: ${t.quantityChange > 0 ? 'green' : 'red'};">
                            ${t.quantityChange > 0 ? '+' : ''}${t.quantityChange}
                        </td>
                        <td style="text-align:right; font-weight:bold;">${t.quantityAfter}</td>
                        <td>${t.createdByName}</td>
                        <td style="font-size:12px; max-width: 150px;">${t.note}</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty transactions}">
                    <tr><td colspan="9" style="text-align:center;">Chưa có lịch sử giao dịch</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<style>
.status-badge { padding: 4px 8px; border-radius: 4px; font-size: 11px; font-weight: bold; }
.status-badge.success { background:#d1fae5; color:#065f46; }
.status-badge.danger { background:#fee2e2; color:#991b1b; }
</style>
