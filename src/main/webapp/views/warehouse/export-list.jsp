<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%--
  export-list.jsp — Danh sách phiếu xuất kho (Có hiển thị Ghi chú)
--%>

<div class="module-heading">
    <div>
        <p class="eyebrow dark">XUẤT KHO</p>
        <h2>Phiếu xuất kho</h2>
        <p>Danh sách tất cả phiếu xuất hàng ra khỏi kho.</p>
    </div>
    <a class="button button-gold"
       href="${pageContext.request.contextPath}/manage/warehouse/export-create">
        ＋ Tạo phiếu xuất
    </a>
</div>

<c:if test="${not empty sessionScope.flashMessage}">
    <div class="flash-message">${sessionScope.flashMessage}</div>
    <c:remove var="flashMessage" scope="session"/>
</c:if>

<div class="dashboard-card">
    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Mã phiếu</th>
                    <th>Loại xuất</th>
                    <th>Người nhận</th>
                    <th>Ngày xuất</th>
                    <th>Ghi chú</th>
                    <th>Người tạo</th>
                    <th>Đơn hàng</th>
                    <th>Trạng thái</th>
                    <th style="text-align:right;">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty exports}">
                        <tr>
                            <td colspan="9" style="text-align:center;">
                                Chưa có phiếu xuất nào.
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${exports}" var="e">
                            <tr>
                                <td><b>${e.exportCode}</b></td>
                                <td>${e.exportTypeLabel}</td>
                                <td>${e.receiverName}</td>
                                <td>${e.exportDate}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty e.note}">
                                            <span style="color:#555; font-style:italic;">${e.note}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color:#aaa;">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${e.createdByName}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty e.orderID}">#${e.orderID}</c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="status-badge ${e.statusClass}">
                                        ${e.statusLabel}
                                    </span>
                                </td>
                                <td style="text-align:right;">
                                    <a class="table-action" 
                                       href="${pageContext.request.contextPath}/manage/warehouse/export-edit?id=${e.exportID}">
                                        ✎ Sửa
                                    </a>
                                    &nbsp;|&nbsp;
                                    <%-- Nút in phiếu xuất --%>
                                    <a class="table-action"
                                       href="${pageContext.request.contextPath}/manage/warehouse/export-print?id=${e.exportID}"
                                       target="_blank">
                                        🖨️ In
                                    </a>
                                    <c:choose>
                                        <c:when test="${e.status != 'COMPLETED'}">
                                            &nbsp;|&nbsp;
                                            <a class="table-action" style="color:var(--color-danger, #e53935);"
                                               href="${pageContext.request.contextPath}/manage/warehouse/export-delete?id=${e.exportID}"
                                               onclick="return confirm('Bạn có chắc muốn xóa phiếu xuất ${e.exportCode} này không?');">
                                                🗑 Xóa
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            &nbsp;|&nbsp;
                                            <span style="color:#888; font-size:0.85rem;" title="Phiếu đã hoàn thành không thể xóa">🔒 Đã chốt</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>
