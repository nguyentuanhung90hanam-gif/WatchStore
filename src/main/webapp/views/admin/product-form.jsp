<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="dashboard-card" style="max-width:960px;margin:0 auto;padding:28px 32px;background:#fff;border:1px solid #e2e8f0;border-radius:12px;box-shadow:0 1px 4px rgba(0,0,0,0.06);">

    <div style="margin-bottom: 24px;">
        <p class="eyebrow dark">QUẢN LÝ SẢN PHẨM</p>
        <h2 style="margin: 0 0 6px 0;">
            ${empty product.productId || product.productId == 0 ? "Thêm Sản Phẩm Mới" : "Chỉnh Sửa Sản Phẩm"}
        </h2>
        <p style="color: #64748b; font-size: 14px; margin: 0;">
            Cập nhật thông tin chi tiết, giá bán, danh mục và bộ sưu tập đồng hồ.
        </p>
    </div>

    <%-- Error Alert --%>
    <c:if test="${not empty errorMessage}">
        <div style="background:#fef2f2;color:#991b1b;border:1px solid #fecaca;border-radius:8px;padding:12px 16px;margin-bottom:20px;font-size:14px;font-weight:600;display:flex;align-items:center;gap:8px;">
            <span>⚠️</span> ${errorMessage}
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/manage/admin/products/${empty product.productId || product.productId == 0 ? 'save' : 'update'}"
          autocomplete="off">

        <c:if test="${not empty product.productId && product.productId > 0}">
            <input type="hidden" name="productId" value="${product.productId}">
        </c:if>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:20px;">

            <%-- Product Code --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    Mã sản phẩm <span style="color:#dc2626;">*</span>
                </label>
                <input type="text"
                       name="productCode"
                       value="${product.productCode}"
                       placeholder="VD: SRPD37J1"
                       style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;text-transform:uppercase;"
                       required>
            </div>

            <%-- Product Name --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    Tên sản phẩm <span style="color:#dc2626;">*</span>
                </label>
                <input type="text"
                       name="productName"
                       value="${product.productName}"
                       placeholder="VD: Seiko Presage Cocktail Time"
                       style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;"
                       required>
            </div>

            <%-- Slug --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    Slug (URL) <span style="color:#dc2626;">*</span>
                </label>
                <input type="text"
                       name="slug"
                       id="slugInput"
                       value="${product.slug}"
                       placeholder="VD: seiko-presage-cocktail-time"
                       style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;"
                       required>
            </div>

            <%-- SKU --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    SKU Biến thể
                </label>
                <input type="text"
                       name="sku"
                       value="${product.sku}"
                       placeholder="VD: SRPD37J1-STD"
                       style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;">
            </div>

            <%-- Price --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    Giá bán (₫) <span style="color:#dc2626;">*</span>
                </label>
                <input type="number"
                       step="any"
                       name="price"
                       value="${product.price}"
                       placeholder="VD: 11500000"
                       style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;"
                       required>
            </div>

            <%-- Brand --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    Thương hiệu <span style="color:#dc2626;">*</span>
                </label>
                <select name="brandId"
                        style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;background:#fff;"
                        required>
                    <c:forEach items="${allBrands}" var="b">
                        <option value="${b.brandID}" ${product.brandId == b.brandID ? 'selected' : ''}>${b.brandName}</option>
                    </c:forEach>
                </select>
            </div>

            <%-- Category --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    Danh mục <span style="color:#dc2626;">*</span>
                </label>
                <select name="categoryId"
                        style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;background:#fff;"
                        required>
                    <c:forEach items="${allCategories}" var="c">
                        <option value="${c.categoryId}" ${product.categoryId == c.categoryId ? 'selected' : ''}>${c.categoryName}</option>
                    </c:forEach>
                </select>
            </div>

            <%-- Movement Type --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    Loại bộ máy
                </label>
                <select name="movementType"
                        style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;background:#fff;">
                    <option value="AUTOMATIC" ${product.movementType == 'AUTOMATIC' ? 'selected' : ''}>AUTOMATIC (Đồng hồ cơ)</option>
                    <option value="QUARTZ" ${product.movementType == 'QUARTZ' ? 'selected' : ''}>QUARTZ (Đồng hồ pin)</option>
                    <option value="SOLAR" ${product.movementType == 'SOLAR' ? 'selected' : ''}>SOLAR (Năng lượng ánh sáng)</option>
                    <option value="SMART" ${product.movementType == 'SMART' ? 'selected' : ''}>SMART (Thông minh)</option>
                    <option value="MECHANICAL" ${product.movementType == 'MECHANICAL' ? 'selected' : ''}>MECHANICAL (Lên cót tay)</option>
                </select>
            </div>

            <%-- Gender --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    Giới tính
                </label>
                <select name="gender"
                        style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;background:#fff;">
                    <option value="MEN" ${product.gender == 'MEN' ? 'selected' : ''}>Nam (MEN)</option>
                    <option value="WOMEN" ${product.gender == 'WOMEN' ? 'selected' : ''}>Nữ (WOMEN)</option>
                    <option value="UNISEX" ${product.gender == 'UNISEX' ? 'selected' : ''}>Unisex (Nam/Nữ)</option>
                    <option value="COUPLE" ${product.gender == 'COUPLE' ? 'selected' : ''}>Đồng hồ đôi (COUPLE)</option>
                </select>
            </div>

            <%-- Status --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    Trạng thái <span style="color:#dc2626;">*</span>
                </label>
                <select name="status"
                        style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;background:#fff;"
                        required>
                    <option value="ACTIVE" ${empty product || product.status == 'ACTIVE' ? 'selected' : ''}>Đang bán (ACTIVE)</option>
                    <option value="DRAFT" ${product.status == 'DRAFT' ? 'selected' : ''}>Bản nháp (DRAFT)</option>
                    <option value="INACTIVE" ${product.status == 'INACTIVE' ? 'selected' : ''}>Tạm ngưng (INACTIVE)</option>
                    <option value="DISCONTINUED" ${product.status == 'DISCONTINUED' ? 'selected' : ''}>Ngừng sản xuất (DISCONTINUED)</option>
                </select>
            </div>

        </div>

        <%-- Featured Checkbox --%>
        <div style="margin-bottom: 20px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px 16px;">
            <label style="display:inline-flex;align-items:center;gap:8px;cursor:pointer;font-weight:600;color:#334155;font-size:14px;">
                <input type="checkbox" name="isFeatured" value="true" ${product.isFeatured ? 'checked' : ''} style="width:18px;height:18px;accent-color:#d4af37;">
                <span>Hiển thị là Sản phẩm nổi bật (Featured Collection)</span>
            </label>
        </div>

        <%-- Description (full width) --%>
        <div style="margin-bottom: 24px;">
            <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                Mô tả chi tiết sản phẩm
            </label>
            <textarea name="description"
                      rows="4"
                      placeholder="Mô tả đặc điểm sản phẩm, phong cách, chất liệu, tính năng nổi bật..."
                      style="width:100%;padding:12px 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;resize:vertical;font-family:inherit;">${product.description}</textarea>
        </div>

        <%-- Action Buttons --%>
        <div style="display:flex;gap:12px;justify-content:flex-end;border-top:1px solid #f1f5f9;padding-top:20px;">
            <a href="${pageContext.request.contextPath}/manage/admin/products"
               class="button button-outline"
               style="padding:10px 22px;border-radius:8px;text-decoration:none;">
                Hủy bỏ
            </a>
            <button type="submit"
                    class="button button-gold"
                    style="padding:10px 28px;border-radius:8px;border:none;cursor:pointer;font-weight:700;font-size:14px;">
                ${empty product.productId || product.productId == 0 ? "💾 Thêm sản phẩm" : "✏️ Lưu thay đổi"}
            </button>
        </div>

    </form>
</div>

<%-- Auto-generate slug from name --%>
<script>
    (function () {
        var nameInput = document.querySelector('input[name="productName"]');
        var slugInput = document.getElementById('slugInput');

        if (nameInput && slugInput && !slugInput.value) {
            nameInput.addEventListener('input', function () {
                slugInput.value = nameInput.value
                    .toLowerCase()
                    .normalize('NFD')
                    .replace(/[\u0300-\u036f]/g, '')
                    .replace(/đ/g, 'd')
                    .replace(/[^a-z0-9\s-]/g, '')
                    .trim()
                    .replace(/\s+/g, '-');
            });
        }
    })();
</script>
