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
                <th>TÊN NHÀ CUNG CẤP</th>
                <th>SỐ PHIẾU NHẬP</th>
                <th>TỔNG GIÁ TRỊ NHẬP</th>
                <th>LẦN NHẬP GẦN NHẤT</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${suppliers}" var="sup">
                <tr>
                    <td><b>${sup.supplierName}</b></td>
                    <td>${sup.receiptCount}</td>
                    <td class="text-bold text-primary">
                        <fmt:formatNumber value="${sup.totalValue}" pattern="#,##0" /> ₫
                    </td>
                    <td>
                        <fmt:formatDate value="${sup.lastReceiptDate}" pattern="dd/MM/yyyy HH:mm" />
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty suppliers}">
                <tr>
                    <td colspan="4" class="text-center text-muted" style="padding: 2rem;">
                        Chưa có dữ liệu nhà cung cấp từ các phiếu nhập.
                    </td>
                </tr>
            </c:if>
        </tbody>
    </table>
</div>
