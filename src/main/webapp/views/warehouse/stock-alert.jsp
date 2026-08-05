<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%--
  stock-alert.jsp — Cảnh báo sắp hết hàng
  Controller đã đẩy vào request:
    - lowStockItems : List<InventoryItem>
      (chỉ chứa những item có availableQuantity <= reorderLevel)
--%>

<div class="module-heading">
    <div>
        <p class="eyebrow dark">CẢNH BÁO TỒN KHO</p>
        <h2>Sản phẩm sắp hết hàng</h2>
        <p>
            Danh sách biến thể có tồn kho khả dụng dưới hoặc bằng ngưỡng cảnh báo.
            Hãy tạo phiếu nhập để bổ sung hàng.
        </p>
    </div>
    <a class="button button-gold"
       href="${pageContext.request.contextPath}/manage/warehouse/receipt-create">
        ⇩ Tạo phiếu nhập bổ sung
    </a>
</div>

<c:choose>
    <c:when test="${empty lowStockItems}">
        <%-- Không có cảnh báo — trạng thái tốt --%>
        <div class="dashboard-card" style="text-align:center; padding:3rem;">
            <p style="font-size:2rem;">✅</p>
            <h3>Tồn kho ổn định</h3>
            <p>Tất cả biến thể đang có tồn kho trên ngưỡng cảnh báo. Không cần hành động.</p>
        </div>
    </c:when>
    <c:otherwise>
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
                            <th>Khả dụng</th>
                            <th>Ngưỡng tối thiểu</th>
                            <th>Mức độ</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${lowStockItems}" var="item">
                            <tr>
                                <td>${item.productCode}</td>
                                <td><b>${item.productName}</b></td>
                                <td>${item.variantSku}</td>
                                <td>${item.variantName}</td>
                                <td>${item.quantityOnHand}</td>
                                <td><b>${item.availableQuantity}</b></td>
                                <td>${item.reorderLevel}</td>
                                <td>
                                    <%--
                                      Phân cấp cảnh báo:
                                        - availableQuantity = 0  → HẾT HÀNG (danger)
                                        - availableQuantity <= reorderLevel → SẮP HẾT (warning)
                                    --%>
                                    <c:choose>
                                        <c:when test="${item.availableQuantity == 0}">
                                            <span class="status-badge danger">Hết hàng</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="status-badge warning">Sắp hết</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:otherwise>
</c:choose>
