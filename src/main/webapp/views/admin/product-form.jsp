<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="dashboard-card" style="max-width:900px;margin:0 auto;padding:24px;">

    <h2>
        ${empty product.productId || product.productId == 0 ? "Thêm sản phẩm" : "Sửa sản phẩm"}
    </h2>

    <c:if test="${not empty errorMessage}">
        <div style="background:#fff3cd;color:#856404;border:1px solid #ffc107;border-radius:8px;padding:12px 16px;margin-bottom:16px;font-size:0.95em;">
            ⚠ ${errorMessage}
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/manage/admin/products/${empty product.productId || product.productId == 0 ? 'save' : 'update'}">

        <c:if test="${not empty product.productId && product.productId > 0}">
            <input type="hidden" name="productId" value="${product.productId}">
        </c:if>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:16px;">
            <div>
                <label>Mã sản phẩm <span style="color:red;">*</span></label>
                <input type="text" name="productCode" value="${product.productCode}" placeholder="VD: SRPD37J1" required style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>
            <div>
                <label>Tên sản phẩm <span style="color:red;">*</span></label>
                <input type="text" name="productName" value="${product.productName}" placeholder="VD: Seiko Presage Cocktail Time" required style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:16px;margin-bottom:16px;">
            <div>
                <label>Slug <span style="color:red;">*</span></label>
                <input type="text" name="slug" value="${product.slug}" placeholder="VD: seiko-presage-cocktail" required style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>
            <div>
                <label>SKU Biến thể</label>
                <input type="text" name="sku" value="${product.sku}" placeholder="VD: SRPD37J1-STD" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>
            <div>
                <label>Giá bán (₫) <span style="color:red;">*</span></label>
                <input type="number" step="any" name="price" value="${product.price}" placeholder="VD: 11500000" required style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr 1fr 1fr;gap:16px;margin-bottom:16px;">
            <div>
                <label>Thương hiệu <span style="color:red;">*</span></label>
                <select name="brandId" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
                    <c:forEach items="${allBrands}" var="b">
                        <option value="${b.brandID}" ${product.brandId == b.brandID ? 'selected' : ''}>${b.brandName}</option>
                    </c:forEach>
                </select>
            </div>
            <div>
                <label>Danh mục <span style="color:red;">*</span></label>
                <select name="categoryId" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
                    <c:forEach items="${allCategories}" var="c">
                        <option value="${c.categoryId}" ${product.categoryId == c.categoryId ? 'selected' : ''}>${c.categoryName}</option>
                    </c:forEach>
                </select>
            </div>
            <div>
                <label>Loại bộ máy</label>
                <select name="movementType" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
                    <option value="AUTOMATIC" ${product.movementType == 'AUTOMATIC' ? 'selected' : ''}>AUTOMATIC (Cơ)</option>
                    <option value="QUARTZ" ${product.movementType == 'QUARTZ' ? 'selected' : ''}>QUARTZ (Pin)</option>
                    <option value="SOLAR" ${product.movementType == 'SOLAR' ? 'selected' : ''}>SOLAR (Ánh sáng)</option>
                    <option value="SMART" ${product.movementType == 'SMART' ? 'selected' : ''}>SMART (Thông minh)</option>
                    <option value="MECHANICAL" ${product.movementType == 'MECHANICAL' ? 'selected' : ''}>MECHANICAL (Lên cót)</option>
                </select>
            </div>
            <div>
                <label>Giới tính</label>
                <select name="gender" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
                    <option value="MEN" ${product.gender == 'MEN' ? 'selected' : ''}>Nam (MEN)</option>
                    <option value="WOMEN" ${product.gender == 'WOMEN' ? 'selected' : ''}>Nữ (WOMEN)</option>
                    <option value="UNISEX" ${product.gender == 'UNISEX' ? 'selected' : ''}>Unisex</option>
                    <option value="COUPLE" ${product.gender == 'COUPLE' ? 'selected' : ''}>Đồng hồ cặp</option>
                </select>
            </div>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:16px;">
            <div>
                <label>Trạng thái</label>
                <select name="status" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
                    <option value="ACTIVE" ${empty product || product.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE (Đang bán)</option>
                    <option value="DRAFT" ${product.status == 'DRAFT' ? 'selected' : ''}>DRAFT (Nháp)</option>
                    <option value="INACTIVE" ${product.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE (Tạm ngưng)</option>
                    <option value="DISCONTINUED" ${product.status == 'DISCONTINUED' ? 'selected' : ''}>DISCONTINUED (Ngừng sản xuất)</option>
                </select>
            </div>
            <div style="display:flex;align-items:center;margin-top:20px;">
                <label style="display:inline-flex;align-items:center;gap:6px;cursor:pointer;">
                    <input type="checkbox" name="isFeatured" value="true" ${product.isFeatured ? 'checked' : ''}>
                    <span>Sản phẩm nổi bật</span>
                </label>
            </div>
        </div>

        <div style="margin-bottom:16px;">
            <label>Mô tả chi tiết</label>
            <textarea name="description" rows="4" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;resize:vertical;" placeholder="Mô tả đặc điểm sản phẩm...">${product.description}</textarea>
        </div>

        <div style="display:flex;gap:12px;margin-top:24px;">
            <button type="submit" class="button button-gold">${empty product.productId || product.productId == 0 ? "Thêm mới" : "Lưu thay đổi"}</button>
            <a href="${pageContext.request.contextPath}/manage/admin/products" class="button" style="background:#eee;color:#333;text-decoration:none;padding:10px 20px;border-radius:6px;">Hủy</a>
        </div>
    </form>
</div>
