<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="dashboard-card" style="max-width:920px;margin:0 auto;padding:28px 32px;background:#fff;border:1px solid #e2e8f0;border-radius:12px;box-shadow:0 1px 4px rgba(0,0,0,0.06);">

    <div style="margin-bottom: 24px;">
        <p class="eyebrow dark">THƯƠNG HIỆU ĐỒNG HỒ</p>
        <h2 style="margin: 0 0 6px 0;">${empty brand.brandID || brand.brandID == 0 ? "Thêm Thương Hiệu Mới" : "Chỉnh Sửa Thương Hiệu"}</h2>
        <p style="color: #64748b; font-size: 14px; margin: 0;">Tạo mới hoặc cập nhật thông tin thương hiệu đồng hồ trong hệ thống WatchStore.</p>
    </div>

    <%-- Error Alert --%>
    <c:if test="${not empty errorMessage}">
        <div style="background:#fef2f2;color:#991b1b;border:1px solid #fecaca;border-radius:8px;padding:12px 16px;margin-bottom:20px;font-size:14px;font-weight:600;display:flex;align-items:center;gap:8px;">
            <span>⚠️</span> ${errorMessage}
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/manage/admin/brands/${empty brand.brandID || brand.brandID == 0 ? 'save' : 'update'}"
          autocomplete="off">

        <%-- Hidden ID for update --%>
        <c:if test="${not empty brand.brandID && brand.brandID != 0}">
            <input type="hidden" name="brandId" value="${brand.brandID}">
        </c:if>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px;">

            <%-- Brand Code --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    Mã thương hiệu <span style="color:#dc2626;">*</span>
                </label>
                <input type="text"
                       name="brandCode"
                       value="${brand.brandCode}"
                       placeholder="VD: ROLEX, CASIO, SEIKO"
                       style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;text-transform:uppercase;"
                       required>
            </div>

            <%-- Brand Name --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    Tên thương hiệu <span style="color:#dc2626;">*</span>
                </label>
                <input type="text"
                       name="brandName"
                       value="${brand.brandName}"
                       placeholder="VD: Rolex, Casio, Seiko"
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
                       value="${brand.slug}"
                       placeholder="VD: rolex, casio-collection"
                       style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;"
                       required>
            </div>

            <%-- Origin Country --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    Quốc gia xuất xứ
                </label>
                <input type="text"
                       name="originCountry"
                       value="${brand.originCountry}"
                       placeholder="VD: Thụy Sĩ, Nhật Bản, Mỹ"
                       style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;">
            </div>

            <%-- Logo URL --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    URL Logo thương hiệu
                </label>
                <input type="text"
                       name="logoUrl"
                       value="${brand.logoUrl}"
                       placeholder="VD: /assets/images/brands/seiko.png"
                       style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;">
            </div>

            <%-- Status --%>
            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                    Trạng thái <span style="color:#dc2626;">*</span>
                </label>
                <select name="status"
                        style="width:100%;height:42px;padding:0 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;background:#fff;"
                        required>
                    <option value="ACTIVE"   ${brand.status == 'ACTIVE'   || empty brand.status ? 'selected' : ''}>Hoạt động (ACTIVE)</option>
                    <option value="INACTIVE" ${brand.status == 'INACTIVE' ? 'selected' : ''}>Tạm ngưng (INACTIVE)</option>
                </select>
            </div>

        </div>

        <%-- Description (full width) --%>
        <div style="margin-bottom: 24px;">
            <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;font-size:14px;">
                Mô tả thương hiệu
            </label>
            <textarea name="description"
                      rows="4"
                      placeholder="Giới thiệu ngắn về lịch sử và đặc điểm của thương hiệu..."
                      style="width:100%;padding:12px 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;box-sizing:border-box;resize:vertical;font-family:inherit;">${brand.description}</textarea>
        </div>

        <%-- Action Buttons --%>
        <div style="display:flex;gap:12px;justify-content:flex-end;border-top:1px solid #f1f5f9;padding-top:20px;">
            <a href="${pageContext.request.contextPath}/manage/admin/brands"
               class="button button-outline"
               style="padding:10px 22px;border-radius:8px;text-decoration:none;">
                Hủy bỏ
            </a>
            <button type="submit"
                    class="button button-gold"
                    style="padding:10px 28px;border-radius:8px;border:none;cursor:pointer;font-weight:700;font-size:14px;">
                ${empty brand.brandID || brand.brandID == 0 ? " Thêm thương hiệu" : " Cập nhật"}
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