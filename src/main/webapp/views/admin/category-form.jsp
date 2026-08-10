<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="form-container" style="max-width: 860px; margin: 0 auto; padding: 24px;">

    <div style="margin-bottom: 24px;">
        <p class="eyebrow dark">PHÂN LOẠI SẢN PHẨM</p>
        <h2>${empty category.categoryId ? "Thêm Danh Mục Mới" : "Chỉnh Sửa Danh Mục"}</h2>
        <p class="module-desc">Tạo mới hoặc cập nhật danh mục sản phẩm cho hệ thống WatchStore.</p>
    </div>

    <%-- Error Alert --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger" style="background:#f8d7da;color:#721c24;border:1px solid #f5c6cb;padding:14px 18px;border-radius:8px;margin-bottom:24px;font-weight:500;">
            ⚠️ ${errorMessage}
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/manage/admin/categories/${empty category.categoryId ? 'save' : 'update'}"
          class="portal-form">

        <%-- Hidden ID for update --%>
        <c:if test="${not empty category.categoryId}">
            <input type="hidden" name="categoryId" value="${category.categoryId}">
        </c:if>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">

            <%-- Category Code --%>
            <div class="form-group">
                <label style="font-weight:600;margin-bottom:6px;display:block;">
                    Mã danh mục <span style="color:red;">*</span>
                </label>
                <input type="text"
                       name="categoryCode"
                       value="${category.categoryCode}"
                       placeholder="VD: CAT-SPORT, CAT-LUXURY"
                       style="text-transform:uppercase;"
                       required>
            </div>

            <%-- Category Name --%>
            <div class="form-group">
                <label style="font-weight:600;margin-bottom:6px;display:block;">
                    Tên danh mục <span style="color:red;">*</span>
                </label>
                <input type="text"
                       name="categoryName"
                       value="${category.categoryName}"
                       placeholder="VD: Đồng hồ thể thao"
                       required>
            </div>

            <%-- Slug --%>
            <div class="form-group">
                <label style="font-weight:600;margin-bottom:6px;display:block;">
                    Slug (URL) <span style="color:red;">*</span>
                </label>
                <input type="text"
                       name="slug"
                       id="slugInput"
                       value="${category.slug}"
                       placeholder="VD: dong-ho-the-thao"
                       required>
            </div>

            <%-- Display Order --%>
            <div class="form-group">
                <label style="font-weight:600;margin-bottom:6px;display:block;">
                    Thứ tự hiển thị <small style="color:#888;font-weight:normal;">(để trống tự động xếp cuối)</small>
                </label>
                <input type="number"
                       name="displayOrder"
                       value="${category.displayOrder}"
                       placeholder="Để trống tự động xếp cuối"
                       min="1">
            </div>

            <%-- Parent Category --%>
            <div class="form-group">
                <label style="font-weight:600;margin-bottom:6px;display:block;">Danh mục cha</label>
                <select name="parentCategoryId">
                    <option value="">-- Không có (danh mục gốc) --</option>
                    <c:forEach items="${allCategories}" var="parent">
                        <c:if test="${parent.categoryId != category.categoryId}">
                            <option value="${parent.categoryId}"
                                ${category.parentCategoryId == parent.categoryId ? 'selected' : ''}>
                                ${parent.categoryName}
                            </option>
                        </c:if>
                    </c:forEach>
                </select>
            </div>

            <%-- Status --%>
            <div class="form-group">
                <label style="font-weight:600;margin-bottom:6px;display:block;">
                    Trạng thái <span style="color:red;">*</span>
                </label>
                <select name="status" required>
                    <option value="ACTIVE"   ${category.status == 'ACTIVE'   || empty category.status ? 'selected' : ''}>Hoạt động (ACTIVE)</option>
                    <option value="INACTIVE" ${category.status == 'INACTIVE' ? 'selected' : ''}>Tạm ngưng (INACTIVE)</option>
                </select>
            </div>

        </div>

        <%-- Image URL (full width) --%>
        <div class="form-group" style="margin-top: 20px;">
            <label style="font-weight:600;margin-bottom:6px;display:block;">URL Ảnh danh mục</label>
            <input type="text"
                   name="imageUrl"
                   value="${category.imageUrl}"
                   placeholder="VD: https://example.com/img/category.jpg">
        </div>

        <%-- Description (full width) --%>
        <div class="form-group" style="margin-top: 20px;">
            <label style="font-weight:600;margin-bottom:6px;display:block;">Mô tả</label>
            <textarea name="description"
                      rows="4"
                      placeholder="Mô tả ngắn gọn về danh mục này...">${category.description}</textarea>
        </div>

        <%-- Buttons --%>
        <div style="display:flex;gap:12px;justify-content:flex-end;margin-top:28px;border-top:1px solid #eee;padding-top:20px;">
            <a href="${pageContext.request.contextPath}/manage/admin/categories"
               class="button"
               style="background:#6c757d;color:#fff;border:none;padding:10px 24px;border-radius:6px;text-decoration:none;cursor:pointer;">
                ← Hủy
            </a>
            <button type="submit"
                    class="button button-gold"
                    style="padding:10px 32px;border-radius:6px;border:none;cursor:pointer;font-weight:700;font-size:1em;">
                ${empty category.categoryId ? "💾 Thêm danh mục" : "✏️ Cập nhật"}
            </button>
        </div>

    </form>

</div>

<%-- Auto-generate slug from name --%>
<script>
    (function () {
        var nameInput = document.querySelector('input[name="categoryName"]');
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
