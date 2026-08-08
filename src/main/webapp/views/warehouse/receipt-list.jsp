<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">NHẬP KHO</p>
        <h2>Phiếu nhập kho</h2>
        <p class="module-desc">Lịch sử và chứng từ nhập kho vào hệ thống.</p>
    </div>
    <a class="button button-gold" href="${cp}/manage/warehouse/receipt-create">+ Tạo phiếu nhập</a>
</div>

<c:if test="${not empty sessionScope.successMsg}">
    <div class="alert alert-success">${sessionScope.successMsg}</div>
    <c:remove var="successMsg" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.errorMsg}">
    <div class="alert alert-error">${sessionScope.errorMsg}</div>
    <c:remove var="errorMsg" scope="session"/>
</c:if>

<div class="dashboard-card">
    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Mã Phiếu</th>
                    <th>Ngày nhập</th>
                    <th>Kho</th>
                    <th>Nhà cung cấp</th>
                    <th>Tổng tiền</th>
                    <th>Trạng thái</th>
                    <th>Người tạo</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${receipts}" var="r">
                    <tr>
                        <td><b>#${r.receiptCode}</b></td>
                        <td>${r.receiptDate}</td>
                        <td>${r.warehouseName}</td>
                        <td>
                            <b>${r.supplierName}</b><br>
                            <small>${r.supplierPhone}</small>
                        </td>
                        <td><fmt:formatNumber value="${r.totalCost}" pattern="#,##0"/>₫</td>
                        <td>
                            <span class="status-badge
                                ${r.status == 'COMPLETED' ? 'success' :
                                  r.status == 'PENDING'   ? 'warning' :
                                  r.status == 'CANCELLED' ? 'neutral' : 'draft'}">
                                ${r.status}
                            </span>
                        </td>
                        <td>${r.createdByName}</td>
                        <td>
                            <a class="button button-outline" style="padding:5px 10px; font-size:12px;"
                               href="${cp}/manage/warehouse/receipt-detail?id=${r.stockReceiptId}">Chi tiết</a>
                            <c:if test="${r.status == 'COMPLETED'}">
                                <a class="button button-gold" style="padding:5px 10px; font-size:12px;"
                                   href="${cp}/manage/warehouse/receipt-pdf?id=${r.stockReceiptId}" target="_blank">PDF</a>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty receipts}">
                    <tr><td colspan="8" style="text-align:center;">Chưa có phiếu nhập nào</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<style>
.alert { padding:12px 16px; border-radius:6px; margin-bottom:16px; }
.alert-success { background:#d4edda; color:#155724; border:1px solid #c3e6cb; }
.alert-error   { background:#f8d7da; color:#721c24; border:1px solid #f5c6cb; }
.status-badge.draft { background:#e8f4fd; color:#2980b9; }
</style>
