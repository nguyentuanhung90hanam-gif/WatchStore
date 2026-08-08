<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="editing" value="${not empty variant}"/>

<div class="module-heading">

    <div class="module-title-area">

        <p class="eyebrow dark">
            BIẾN THỂ SẢN PHẨM
        </p>

        <h2>
            ${editing ? 'Chỉnh sửa biến thể' : 'Thêm biến thể'}
        </h2>

        <p class="module-desc">
            Nhập thông tin SKU, giá và trạng thái của biến thể.
        </p>

    </div>

    <div>

        <a href="${cp}/manage/warehouse/variants"
           class="btn">
            Quay lại
        </a>

    </div>

</div>

<c:if test="${not empty errorMsg}">

    <div class="alert alert-danger">
        ${errorMsg}
    </div>

</c:if>

<div class="dashboard-card">

    <form method="post"
          action="${cp}/manage/warehouse/${editing ? 'variant-update' : 'variant-create'}">

        <c:if test="${editing}">

            <input type="hidden"
                   name="variantId"
                   value="${variant.variantId}">

        </c:if>

        <div class="form-grid">

            <div class="form-group">

                <label for="productId">
                    Sản phẩm <span>*</span>
                </label>

                <select id="productId"
                        name="productId"
                        required>

                    <option value="">
                        -- Chọn sản phẩm --
                    </option>

                    <c:forEach items="${productOptions}"
                               var="product">

                        <option value="${product.productId}"
                                ${variant.productId == product.productId ? 'selected' : ''}>

                            ${product.productName}
                            -
                            ${product.brandName}

                        </option>

                    </c:forEach>

                </select>

            </div>

            <div class="form-group">

                <label for="variantName">
                    Tên biến thể <span>*</span>
                </label>

                <input type="text"
                       id="variantName"
                       name="variantName"
                       value="${variant.variantName}"
                       maxlength="150"
                       required>

            </div>

            <div class="form-group">

                <label for="sku">
                    SKU <span>*</span>
                </label>

                <input type="text"
                       id="sku"
                       name="sku"
                       value="${variant.sku}"
                       maxlength="80"
                       required>

            </div>

            <div class="form-group">

                <label for="barcode">
                    Barcode
                </label>

                <input type="text"
                       id="barcode"
                       name="barcode"
                       value="${variant.barcode}"
                       maxlength="80">

            </div>

            <div class="form-group">

                <label for="costPrice">
                    Giá nhập <span>*</span>
                </label>

                <input type="number"
                       id="costPrice"
                       name="costPrice"
                       value="${variant.costPrice}"
                       min="0"
                       step="0.01"
                       required>

            </div>

            <div class="form-group">

                <label for="salePrice">
                    Giá bán <span>*</span>
                </label>

                <input type="number"
                       id="salePrice"
                       name="salePrice"
                       value="${variant.salePrice}"
                       min="0"
                       step="0.01"
                       required>

            </div>

            <div class="form-group">

                <label for="compareAtPrice">
                    Giá niêm yết
                </label>

                <input type="number"
                       id="compareAtPrice"
                       name="compareAtPrice"
                       value="${variant.compareAtPrice}"
                       min="0"
                       step="0.01">

            </div>

            <div class="form-group">

                <label for="weightGram">
                    Trọng lượng (gram)
                </label>

                <input type="number"
                       id="weightGram"
                       name="weightGram"
                       value="${variant.weightGram}"
                       min="0"
                       step="1">

            </div>

            <div class="form-group">

                <label for="status">
                    Trạng thái
                </label>

                <select id="status"
                        name="status">

                    <option value="ACTIVE"
                            ${empty variant.status || variant.status == 'ACTIVE' ? 'selected' : ''}>
                        Đang hoạt động
                    </option>

                    <option value="INACTIVE"
                            ${variant.status == 'INACTIVE' ? 'selected' : ''}>
                        Ngừng sử dụng
                    </option>

                    <option value="OUT_OF_STOCK"
                            ${variant.status == 'OUT_OF_STOCK' ? 'selected' : ''}>
                        Hết hàng
                    </option>

                </select>

            </div>

        </div>

        <div style="margin-top:24px; display:flex; gap:10px;">

            <button type="submit"
                    class="btn btn-primary">

                ${editing ? 'Cập nhật biến thể' : 'Thêm biến thể'}

            </button>

            <a href="${cp}/manage/warehouse/variants"
               class="btn">

                Hủy

            </a>

        </div>

    </form>

</div>