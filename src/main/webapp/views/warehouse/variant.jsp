<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">BIẾN THỂ SẢN PHẨM</p>
        <h2>Biến thể & Thuộc tính</h2>
        <p class="module-desc">
            Quản lý SKU, Barcode, giá bán và thuộc tính của từng sản phẩm.
        </p>
    </div>

    <div>
        <a href="${cp}/manage/warehouse/variant-form"
           class="btn btn-primary">
            + Thêm biến thể
        </a>
    </div>
</div>

<c:if test="${not empty sessionScope.successMsg}">
    <div class="alert alert-success">
        ${sessionScope.successMsg}
    </div>
    <c:remove var="successMsg" scope="session"/>
</c:if>

<c:if test="${not empty sessionScope.errorMsg}">
    <div class="alert alert-danger">
        ${sessionScope.errorMsg}
    </div>
    <c:remove var="errorMsg" scope="session"/>
</c:if>

<div class="module-toolbar">

    <form method="get"
          action="${cp}/manage/warehouse/variants"
          class="search-box">

        <input type="text"
               name="keyword"
               value="${keyword}"
               placeholder="SKU, Barcode, tên biến thể hoặc sản phẩm">

        <select name="status">
            <option value="">Tất cả trạng thái</option>

            <option value="ACTIVE"
                    ${status == 'ACTIVE' ? 'selected' : ''}>
                Đang hoạt động
            </option>

            <option value="INACTIVE"
                    ${status == 'INACTIVE' ? 'selected' : ''}>
                Ngừng sử dụng
            </option>

            <option value="OUT_OF_STOCK"
                    ${status == 'OUT_OF_STOCK' ? 'selected' : ''}>
                Hết hàng
            </option>
        </select>

        <button type="submit">
            Tìm kiếm
        </button>

        <a href="${cp}/manage/warehouse/variants">
            Xóa lọc
        </a>
    </form>

</div>

<div class="dashboard-card">

    <div class="table-wrap">

        <table>

            <thead>
                <tr>
                    <th>Sản phẩm</th>
                    <th>Biến thể</th>
                    <th>SKU / Barcode</th>
                    <th>Thuộc tính</th>
                    <th>Giá nhập</th>
                    <th>Giá bán</th>
                    <th>Trọng lượng</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
            </thead>

            <tbody>

                <c:forEach items="${variants}" var="v">

                    <tr>

                        <td>
                            <b>${v.productName}</b>
                            <br>
                            <small>${v.brandName}</small>
                        </td>

                        <td>
                            ${v.variantName}
                        </td>

                        <td>
                            <b>${v.sku}</b>

                            <c:if test="${not empty v.barcode}">
                                <br>
                                <small>${v.barcode}</small>
                            </c:if>
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${not empty v.attributes}">
                                    ${v.attributes}
                                </c:when>

                                <c:otherwise>
                                    <span style="color:#999;">
                                        Chưa có thuộc tính
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <fmt:formatNumber
                                    value="${v.costPrice}"
                                    pattern="#,##0"/>₫
                        </td>

                        <td>
                            <fmt:formatNumber
                                    value="${v.salePrice}"
                                    pattern="#,##0"/>₫
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${not empty v.weightGram}">
                                    ${v.weightGram}g
                                </c:when>

                                <c:otherwise>
                                    -
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td>

                            <c:choose>

                                <c:when test="${v.status == 'ACTIVE'}">
                                    <span class="status-badge success">
                                        Đang hoạt động
                                    </span>
                                </c:when>

                                <c:when test="${v.status == 'OUT_OF_STOCK'}">
                                    <span class="status-badge warning">
                                        Hết hàng
                                    </span>
                                </c:when>

                                <c:otherwise>
                                    <span class="status-badge warning">
                                        Ngừng sử dụng
                                    </span>
                                </c:otherwise>

                            </c:choose>

                        </td>

                        <td>

                            <div style="display:flex; gap:6px; flex-wrap:wrap;">

                                <a href="${cp}/manage/warehouse/variant-form?id=${v.variantId}"
                                   class="btn btn-sm">
                                    Sửa
                                </a>

                                <c:if test="${v.status == 'ACTIVE'}">

                                    <form method="post"
                                          action="${cp}/manage/warehouse/variant-status"
                                          style="display:inline;">

                                        <input type="hidden"
                                               name="variantId"
                                               value="${v.variantId}">

                                        <input type="hidden"
                                               name="status"
                                               value="INACTIVE">

                                        <button type="submit"
                                                class="btn btn-sm"
                                                onclick="return confirm('Bạn có chắc muốn ngừng sử dụng biến thể này?');">
                                            Ngừng dùng
                                        </button>

                                    </form>

                                </c:if>

                                <c:if test="${v.status == 'INACTIVE'}">

                                    <form method="post"
                                          action="${cp}/manage/warehouse/variant-status"
                                          style="display:inline;">

                                        <input type="hidden"
                                               name="variantId"
                                               value="${v.variantId}">

                                        <input type="hidden"
                                               name="status"
                                               value="ACTIVE">

                                        <button type="submit"
                                                class="btn btn-sm">
                                            Kích hoạt
                                        </button>

                                    </form>

                                </c:if>

                                <form method="post"
                                      action="${cp}/manage/warehouse/variant-delete"
                                      style="display:inline;">

                                    <input type="hidden"
                                           name="variantId"
                                           value="${v.variantId}">

                                    <button type="submit"
                                            class="btn btn-sm"
                                            onclick="return confirm('Xóa biến thể này? Nếu biến thể đã phát sinh dữ liệu nghiệp vụ, hệ thống sẽ tự chuyển sang INACTIVE.');">
                                        Xóa
                                    </button>

                                </form>

                            </div>

                        </td>

                    </tr>

                </c:forEach>

                <c:if test="${empty variants}">

                    <tr>

                        <td colspan="9"
                            style="text-align:center; padding:30px;">

                            Không tìm thấy biến thể nào.

                        </td>

                    </tr>

                </c:if>

            </tbody>

        </table>

    </div>

</div>