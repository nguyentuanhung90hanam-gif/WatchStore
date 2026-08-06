<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- variant-list.jsp — Quản lý biến thể sản phẩm (Xem, Thêm, Sửa, Xóa) --%>

<div class="module-heading">
    <div>
        <p class="eyebrow dark">BIẾN THỂ</p>
        <h2>Danh sách biến thể sản phẩm</h2>
        <p>Quản lý SKU, màu sắc, chất liệu dây, giá bán và tồn kho từng biến thể.</p>
    </div>
    <a href="${pageContext.request.contextPath}/manage/warehouse/variant-create"
       class="button button-gold">＋ Thêm biến thể</a>
</div>

<%-- Flash message --%>
<c:if test="${not empty sessionScope.flashMessage}">
    <div class="flash-message">${sessionScope.flashMessage}</div>
    <c:remove var="flashMessage" scope="session"/>
</c:if>

<div class="dashboard-card">
    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>SKU</th>
                    <th>Tên biến thể</th>
                    <th>Sản phẩm</th>
                    <th>Màu sắc</th>
                    <th>Chất liệu</th>
                    <th>Giá bán</th>
                    <th>Tồn kho</th>
                    <th>Trạng thái</th>
                    <th style="text-align:right;">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty variants}">
                        <tr>
                            <td colspan="9" style="text-align:center;">Chưa có biến thể nào.</td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${variants}" var="v">
                            <tr>
                                <td><b>${v.sku}</b></td>
                                <td>${v.variantName}</td>
                                <td>
                                    <small style="color:var(--color-muted)">${v.productCode}</small><br>
                                    ${v.productName}
                                </td>
                                <td>${v.color}</td>
                                <td>${v.material}</td>
                                <td>
                                    <fmt:formatNumber value="${v.price}" type="number" maxFractionDigits="0"/> đ
                                </td>
                                <td>
                                    <%-- Tô đỏ nếu tồn kho = 0 --%>
                                    <c:choose>
                                        <c:when test="${v.stockQty == 0}">
                                            <span style="color:var(--color-danger, #e53935);font-weight:bold;">Hết hàng</span>
                                        </c:when>
                                        <c:otherwise>
                                            ${v.stockQty}
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="status-badge ${v.statusClass}">${v.statusLabel}</span>
                                </td>
                                <td style="text-align:right;">
                                    <a class="table-action"
                                       href="${pageContext.request.contextPath}/manage/warehouse/variant-edit?id=${v.variantID}">
                                         Sửa
                                    </a>
                                    &nbsp;
                                    <a class="table-action" style="color:var(--color-danger, #e53935);"
                                       href="${pageContext.request.contextPath}/manage/warehouse/variant-delete?id=${v.variantID}"
                                       onclick="return confirm('Xóa biến thể ${v.sku}? (Chỉ xóa được nếu tồn kho = 0)')">
                                         Xóa
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>
