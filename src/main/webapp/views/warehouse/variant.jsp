<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">BIẾN THỂ SẢN PHẨM</p>
        <h2>Biến thể & Thuộc tính</h2>
        <p class="module-desc">Quản lý SKU, giá và các thuộc tính động từ database.</p>
    </div>
</div>

<div class="module-toolbar">
    <div class="search-box">
        <input type="text" placeholder="Tìm kiếm theo SKU hoặc Tên">
        <button type="button">Tìm kiếm</button>
    </div>
</div>

<div class="dashboard-card">
    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Sản phẩm</th>
                    <th>Biến thể</th>
                    <th>SKU / Barcode</th>
                    <th>Thuộc tính (JOIN)</th>
                    <th>Giá bán</th>
                    <th>Trọng lượng</th>
                    <th>Trạng thái</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${variants}" var="v">
                    <tr>
                        <td>
                            <b>${v.productName}</b><br>
                            <small>${v.brandName}</small>
                        </td>
                        <td>${v.variantName}</td>
                        <td>
                            <b>${v.sku}</b><br>
                            <small>${v.barcode}</small>
                        </td>
                        <td>${v.attributes}</td>
                        <td><fmt:formatNumber value="${v.salePrice}" pattern="#,##0" />₫</td>
                        <td>${v.weightGram}g</td>
                        <td>
                            <span class="status-badge ${v.status == 'ACTIVE' ? 'success' : 'warning'}">
                                ${v.status}
                            </span>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty variants}">
                    <tr>
                        <td colspan="7" style="text-align: center;">Chưa có biến thể nào</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>
