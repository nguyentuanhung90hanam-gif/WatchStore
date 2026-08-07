<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<div class="admin-form-head">
    <a href="${cp}/manage/admin/brands" aria-label="Quay lại danh sách">
        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m15 5-7 7 7 7"/></svg>
    </a>
    <div>
        <p class="admin-eyebrow">THƯƠNG HIỆU / ${empty brand ? 'TẠO MỚI' : 'CHỈNH SỬA'}</p>
        <h2>${empty brand ? 'Thêm thương hiệu mới' : 'Cập nhật thương hiệu'}</h2>
        <p>${empty brand ? 'Tạo hồ sơ nhận diện cho một thương hiệu đồng hồ.' : 'Chỉnh sửa thông tin và trạng thái hiển thị của thương hiệu.'}</p>
    </div>
</div>

<form class="brand-form-layout" method="post" action="${cp}/manage/admin/brands" data-brand-form>
    <input type="hidden" name="id" value="${brand.brandID}">

    <div class="brand-form-main">
        <section class="admin-form-card">
            <div class="admin-form-card-head">
                <span>01</span>
                <div><h3>Thông tin nhận diện</h3><p>Tên và mã duy nhất dùng xuyên suốt hệ thống.</p></div>
            </div>
            <div class="admin-form-grid two">
                <label>
                    <span>Mã thương hiệu <b>*</b></span>
                    <input type="text" name="brandCode" value="${brand.brandCode}" placeholder="VD: SEIKO" maxlength="30" autocomplete="off" required data-code-input>
                    <small>Viết hoa, không dấu và không chứa khoảng trắng.</small>
                </label>
                <label>
                    <span>Tên thương hiệu <b>*</b></span>
                    <input type="text" name="brandName" value="${brand.brandName}" placeholder="VD: Seiko" maxlength="100" autocomplete="off" required data-brand-name>
                </label>
                <label class="admin-form-full">
                    <span>Slug đường dẫn</span>
                    <div class="input-prefix"><i>/thuong-hieu/</i><input type="text" name="slug" value="${brand.slug}" placeholder="seiko" maxlength="120" data-slug-input></div>
                    <small>Tự động tạo từ tên thương hiệu, bạn vẫn có thể chỉnh lại.</small>
                </label>
            </div>
        </section>

        <section class="admin-form-card">
            <div class="admin-form-card-head">
                <span>02</span>
                <div><h3>Hồ sơ thương hiệu</h3><p>Thông tin bổ sung phục vụ trang danh mục và tìm kiếm.</p></div>
            </div>
            <div class="admin-form-grid">
                <label>
                    <span>Quốc gia xuất xứ</span>
                    <input type="text" name="originCountry" value="${brand.originCountry}" placeholder="VD: Nhật Bản" maxlength="80">
                </label>
                <label>
                    <span>Logo URL</span>
                    <input type="url" name="logoUrl" value="${brand.logoUrl}" placeholder="https://example.com/logo.png" data-logo-input>
                    <small>Nên dùng ảnh PNG hoặc SVG nền trong suốt, tỷ lệ ngang.</small>
                </label>
                <label>
                    <span>Mô tả</span>
                    <textarea name="description" rows="6" maxlength="1000" placeholder="Giới thiệu ngắn về lịch sử, phong cách và điểm nổi bật..." data-description-input>${brand.description}</textarea>
                    <small class="field-counter"><span data-description-count>0</span>/1000 ký tự</small>
                </label>
            </div>
        </section>
    </div>

    <aside class="brand-form-side">
        <section class="admin-form-card brand-preview-card">
            <div class="admin-form-card-head compact"><div><h3>Xem trước</h3><p>Hiển thị trong danh sách quản trị</p></div></div>
            <div class="brand-preview">
                <div class="brand-preview-logo" data-logo-preview>
                    <c:choose>
                        <c:when test="${not empty brand.logoUrl}"><img src="${brand.logoUrl}" alt=""></c:when>
                        <c:otherwise><img alt="" hidden></c:otherwise>
                    </c:choose>
                    <span data-logo-fallback>${empty brand.brandName ? 'W' : fn:toUpperCase(fn:substring(brand.brandName, 0, 1))}</span>
                </div>
                <small>THƯƠNG HIỆU CHÍNH HÃNG</small>
                <b data-name-preview>${empty brand.brandName ? 'Tên thương hiệu' : brand.brandName}</b>
                <span data-code-preview>${empty brand.brandCode ? 'MÃ THƯƠNG HIỆU' : brand.brandCode}</span>
            </div>
        </section>

        <section class="admin-form-card">
            <div class="admin-form-card-head compact"><div><h3>Trạng thái</h3><p>Kiểm soát khả năng hiển thị</p></div></div>
            <div class="status-options">
                <label>
                    <input type="radio" name="status" value="ACTIVE" ${empty brand || brand.status == 'ACTIVE' ? 'checked' : ''}>
                    <span><i></i><b>Đang hoạt động</b><small>Hiển thị trên cửa hàng và cho phép gắn sản phẩm.</small></span>
                </label>
                <label>
                    <input type="radio" name="status" value="INACTIVE" ${brand.status == 'INACTIVE' ? 'checked' : ''}>
                    <span><i></i><b>Tạm ngừng</b><small>Ẩn khỏi cửa hàng nhưng vẫn giữ nguyên dữ liệu.</small></span>
                </label>
            </div>
        </section>

        <div class="brand-form-actions">
            <a href="${cp}/manage/admin/brands">Hủy bỏ</a>
            <button type="submit">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 4h12l2 2v14H5zM8 4v6h8V4M8 20v-6h8v6"/></svg>
                ${empty brand ? 'Tạo thương hiệu' : 'Lưu thay đổi'}
            </button>
        </div>
    </aside>
</form>
