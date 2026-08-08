<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">XUẤT KHO</p>
        <h2>Phiếu xuất kho</h2>
        <p class="module-desc">Lịch sử xuất bán, chuyển kho và xuất hủy.</p>
    </div>
    <a class="button button-gold" href="${cp}/manage/warehouse/export-create">+ Tạo phiếu xuất</a>
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
                    <th>Ngày xuất</th>
                    <th>Loại</th>
                    <th>Kho xuất</th>
                    <th>Người nhận</th>
                    <th>Mã Đơn hàng</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${exports}" var="e">
                    <tr>
                        <td><b>#${e.exportCode}</b></td>
                        <td>${e.exportDate}</td>
                        <td><b>${e.exportType}</b></td>
                        <td>${e.warehouseName}</td>
                        <td>${e.receiverName}</td>
                        <td>${e.orderId != null ? e.orderId : '—'}</td>
                        <td>
                            <span class="status-badge
                                ${e.status == 'COMPLETED' ? 'success' :
                                  e.status == 'PENDING'   ? 'warning' :
                                  e.status == 'CANCELLED' ? 'neutral' : 'draft'}">
                                ${e.status}
                            </span>
                        </td>
                        <td>
                            <a class="button button-outline" style="padding:5px 10px; font-size:12px;"
                               href="${cp}/manage/warehouse/export-detail?id=${e.stockExportId}">Chi tiết</a>
                            <c:if test="${e.status == 'COMPLETED'}">
                                <a class="button button-gold" style="padding:5px 10px; font-size:12px;"
                                   href="${cp}/manage/warehouse/export-pdf?id=${e.stockExportId}" target="_blank">PDF</a>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty exports}">
                    <tr><td colspan="8" style="text-align:center;">Chưa có phiếu xuất nào</td></tr>
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