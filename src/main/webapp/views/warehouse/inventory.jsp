<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>

<%--
  inventory.jsp — Tồn kho theo biến thể sản phẩm (Có tính năng Điều chỉnh số lượng)
--%>

<div class="module-heading">
    <div>
        <p class="eyebrow dark">TỒN KHO</p>
        <h2>Quản lý tồn kho</h2>
        <p>Tồn kho thực tế, số lượng đang giữ và điều chỉnh số lượng tồn kho.</p>
    </div>
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
                    <th>Mã sản phẩm</th>
                    <th>Tên sản phẩm</th>
                    <th>SKU</th>
                    <th>Biến thể</th>
                    <th>Tồn thực tế</th>
                    <th>Đang giữ</th>
                    <th>Khả dụng</th>
                    <th>Cảnh báo min</th>
                    <th>Trạng thái</th>
                    <th style="text-align:right;">Chỉnh sửa</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty inventoryItems}">
                        <tr>
                            <td colspan="10" style="text-align:center;">
                                Không có dữ liệu tồn kho.
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${inventoryItems}" var="item">
                            <tr>
                                <td>${item.productCode}</td>
                                <td><b>${item.productName}</b></td>
                                <td><code>${item.variantSku}</code></td>
                                <td>${item.variantName}</td>
                                <td><b>${item.quantityOnHand}</b></td>
                                <td>${item.quantityReserved}</td>
                                <td><b>${item.availableQuantity}</b></td>
                                <td>${item.reorderLevel}</td>
                                <td>
                                    <span class="status-badge ${item.lowStock ? 'warning' : 'success'}">
                                        ${item.lowStock ? 'Sắp hết' : 'Ổn định'}
                                    </span>
                                </td>
                                <td style="text-align:right;">
                                    <%-- Form sửa nhanh tồn kho ngay trên từng dòng --%>
                                    <form action="${pageContext.request.contextPath}/manage/warehouse/inventory-edit"
                                          method="post" style="display:inline-flex; gap:4px; align-items:center;">
                                        <input type="hidden" name="sku" value="${item.variantSku}" />
                                        <input type="number" name="quantityOnHand" value="${item.quantityOnHand}" 
                                               style="width:60px; padding:2px 4px; text-align:center;" title="Số lượng tồn kho" />
                                        <input type="number" name="reorderLevel" value="${item.reorderLevel}" 
                                               style="width:50px; padding:2px 4px; text-align:center;" title="Ngưỡng cảnh báo" />
                                        <button type="submit" class="table-action" style="padding:2px 8px;">
                                            Lưu
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>
