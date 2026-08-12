<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="form-container" style="max-width: 860px; margin: 0 auto; padding: 24px;">

    <div style="margin-bottom: 24px;">
        <p class="eyebrow dark">THƯƠNG HIỆU ĐỒNG HỒ</p>
        <h2>${empty brand.brandID || brand.brandID == 0 ? "Thêm Thương Hiệu Mới" : "Chỉnh Sửa Thương Hiệu"}</h2>
        <p class="module-desc">Tạo mới hoặc cập nhật thông tin thương hiệu đồng hồ trong hệ thống WatchStore.</p>
    </div>

    <%-- Error Alert --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger"
             style="background:#f8d7da;color:#721c24;border:1px solid #f5c6cb;padding:14px 18px;border-radius:8px;margin-bottom:24px;font-weight:500;">
            ⚠️ ${errorMessage}
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/manage/admin/brands/${empty brand.brandID || brand.brandID == 0 ? 'save' : 'update'}"
          class="portal-form">

        <%-- Hidden ID for update --%>
        <c:if test="${not empty brand.brandID && brand.brandID != 0}">
            <input type="hidden" name="brandId" value="${brand.brandID}">
        </c:if>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">

            <%-- Brand Code --%>
            <div class="form-group">
                <label style="font-weight:600;margin-bottom:6px;display:block;">
                    Mã thương hiệu <span style="color:red;">*</span>
                </label>
                <input type="text"
                       name="brandCode"
                       value="${brand.brandCode}"
                       placeholder="VD: ROLEX, CASIO, SEIKO"
                       style="text-transform:uppercase;"
                       required>
            </div>

            <%-- Brand Name --%>
            <div class="form-group">
                <label style="font-weight:600;margin-bottom:6px;display:block;">
                    Tên thương hiệu <span style="color:red;">*</span>
                </label>
                <input type="text"
                       name="brandName"
                       value="${brand.brandName}"
                       placeholder="VD: Rolex, Casio, Seiko"
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
                       value="${brand.slug}"
                       placeholder="VD: rolex, casio-collection"
                       required>
            </div>

            <%-- Origin Country --%>
            <div class="form-group">
                <label style="font-weight:600;margin-bottom:6px;display:block;">Quốc gia xuất xứ</label>
                <input type="text"
                       name="originCountry"
                       value="${brand.originCountry}"
                       placeholder="VD: Thụy Sĩ, Nhật Bản, Mỹ">
            </div>

            <%-- Logo URL --%>
            <div class="form-group">
                <label style="font-weight:600;margin-bottom:6px;display:block;">URL Logo thương hiệu</label>
                <input type="text"
                       name="logoUrl"
                       value="${brand.logoUrl}"
                       placeholder="VD: https://example.com/logo/rolex.png">
            </div>

            <%-- Status --%>
            <div class="form-group">
                <label style="font-weight:600;margin-bottom:6px;display:block;">
                    Trạng thái <span style="color:red;">*</span>
                </label>
                <select name="status" required>
                    <option value="ACTIVE"   ${brand.status == 'ACTIVE'   || empty brand.status ? 'selected' : ''}>Hoạt động (ACTIVE)</option>
                    <option value="INACTIVE" ${brand.status == 'INACTIVE' ? 'selected' : ''}>Tạm ngưng (INACTIVE)</option>
                </select>
            </div>

        </div>

        <%-- Description (full width) --%>
        <div class="form-group" style="margin-top: 20px;">
            <label style="font-weight:600;margin-bottom:6px;display:block;">Mô tả thương hiệu</label>
            <textarea name="description"
                      rows="4"
                      placeholder="Giới thiệu ngắn về lịch sử và đặc điểm của thương hiệu...">${brand.description}</textarea>
        </div>

        <%-- Buttons --%>
        <div style="display:flex;gap:12px;justify-content:flex-end;margin-top:28px;border-top:1px solid #eee;padding-top:20px;">
            <a href="${pageContext.request.contextPath}/manage/admin/brands"
               class="button"
               style="background:#6c757d;color:#fff;border:none;padding:10px 24px;border-radius:6px;text-decoration:none;">
                ← Hủy
            </a>
            <button type="submit"
                    class="button button-gold"
                    style="padding:10px 32px;border-radius:6px;border:none;cursor:pointer;font-weight:700;font-size:1em;">
                ${empty brand.brandID || brand.brandID == 0 ? "💾 Thêm thương hiệu" : "✏️ Cập nhật"}
            </button>
        </div>

    </form>
</div>

<%-- Auto slug from brand name --%>
<script>
    (function () {
        var nameInput = document.querySelector('input[name="brandName"]');
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