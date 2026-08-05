<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%--
  receipt-list.jsp — Danh sách phiếu nhập kho (Hiển thị đầy đủ Ghi chú)
--%>

<div class="module-heading">
    <div>
        <p class="eyebrow dark">NHẬP KHO</p>
        <h2>Phiếu nhập kho</h2>
        <p>Danh sách tất cả phiếu nhập hàng từ nhà cung cấp.</p>
    </div>
    <a class="button button-gold" href="${pageContext.request.contextPath}/manage/warehouse/receipt-create">
        ＋ Tạo phiếu nhập
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
                    <th>Nhà cung cấp</th>
                    <th>Số điện thoại</th>
                    <th>Ngày nhập</th>
                    <th>Tổng giá trị</th>
                    <th>Ghi chú</th>
                    <th>Người tạo</th>
                    <th>Trạng thái</th>
                    <th style="text-align:right;">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty receipts}">
                        <tr>
                            <td colspan="9" style="text-align:center;">
                                Chưa có phiếu nhập nào. Hãy tạo phiếu nhập đầu tiên.
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${receipts}" var="r">
                            <tr>
                                <td><b>${r.receiptCode}</b></td>
                                <td><b>${r.supplierName}</b></td>
                                <td>${r.supplierPhone}</td>
                                <td>${r.receiptDate}</td>
                                <td>
                                    <b><fmt:formatNumber value="${r.totalCost}" pattern="#,##0" />₫</b>
                                </td>
                                <%-- Cột Ghi chú --%>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty r.note}">
                                            <span style="color:#555; font-style:italic;">${r.note}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color:#aaa;">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${r.createdByName}</td>
                                <td>
                                    <span class="status-badge ${r.statusClass}">
                                        ${r.statusLabel}
                                    </span>
                                </td>
                                <td style="text-align:right;">
                                    <a class="table-action" 
                                       href="${pageContext.request.contextPath}/manage/warehouse/receipt-edit?id=${r.receiptID}">
                                        ✎ Sửa
                                    </a>
                                    
                                    <c:choose>
                                        <c:when test="${r.status != 'COMPLETED'}">
                                            &nbsp;|&nbsp;
                                            <a class="table-action" style="color:var(--color-danger, #e53935);"
                                               href="${pageContext.request.contextPath}/manage/warehouse/receipt-delete?id=${r.receiptID}"
                                               onclick="return confirm('Bạn có chắc chắn muốn xóa phiếu nhập ${r.receiptCode} này không?');">
                                                🗑 Xóa
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            &nbsp;|&nbsp;
                                            <span style="color:#888; font-size:0.85rem;" title="Phiếu đã hoàn thành không thể xóa để bảo vệ tồn kho">🔒 Đã chốt</span>
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
