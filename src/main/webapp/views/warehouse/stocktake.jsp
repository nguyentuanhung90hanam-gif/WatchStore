<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">KIỂM KÊ KHO</p>
        <h2>Danh sách Phiếu Kiểm Kê</h2>
        <p class="module-desc">Lịch sử và chứng từ kiểm đếm hàng hóa kho.</p>
    </div>
    <a class="button button-gold" href="${cp}/manage/warehouse/stocktake-create">+ Tạo phiếu kiểm kê</a>
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
                    <th>Kho kiểm kê</th>
                    <th>Ngày kiểm kê</th>
                    <th>Người tạo</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${stocktakes}" var="s">
                    <tr>
                        <td><b>#${s.stocktakeCode}</b></td>
                        <td>${s.warehouseName}</td>
                        <td>${s.stocktakeDate}</td>
                        <td>${s.createdByName}</td>
                        <td>
                            <span class="status-badge
                                ${s.status == 'COMPLETED' ? 'success' :
                                  s.status == 'COUNTING'  ? 'warning' :
                                  s.status == 'CANCELLED' ? 'neutral' : 'draft'}">
                                ${s.status}
                            </span>
                        </td>
                        <td>
                            <a class="button button-outline" style="padding:5px 10px; font-size:12px;"
                               href="${cp}/manage/warehouse/stocktake-detail?id=${s.stocktakeId}">Chi tiết</a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty stocktakes}">
                    <tr><td colspan="6" style="text-align:center;">Chưa có phiếu kiểm kê nào</td></tr>
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
