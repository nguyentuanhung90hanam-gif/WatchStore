<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />

<div class="content-header">
    <div class="header-left">
        <h2>${moduleTitle}</h2>
        <p class="text-muted">${moduleDescription}</p>
    </div>
</div>

<div class="table-card">
    <table class="data-table">
        <thead>
            <tr>
                <th>MÃ YÊU CẦU</th>
                <th>MÃ ĐƠN HÀNG</th>
                <th>KHÁCH HÀNG</th>
                <th>NGÀY TẠO</th>
                <th>LÝ DO</th>
                <th>TRẠNG THÁI</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${warranties}" var="war">
                <tr>
                    <td><b>${war.returnCode}</b></td>
                    <td><a href="${cp}/manage/sales/orders?keyword=${war.orderCode}">${war.orderCode}</a></td>
                    <td>${war.customerName}</td>
                    <td>
                        <fmt:formatDate value="${war.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                    </td>
                    <td>${war.reason}</td>
                    <td>
                        <span class="status-badge ${war.status == 'COMPLETED' ? 'success' : war.status == 'PENDING' ? 'warning' : 'info'}">${war.status}</span>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty warranties}">
                <tr>
                    <td colspan="6" class="text-center text-muted" style="padding: 2rem;">
                        Không có yêu cầu bảo hành nào.
                    </td>
                </tr>
            </c:if>
        </tbody>
    </table>
</div>
