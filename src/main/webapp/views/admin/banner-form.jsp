<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="dashboard-card" style="max-width:800px;margin:0 auto;padding:24px;">

    <h2>
        ${empty banner.bannerId || banner.bannerId == 0 ? "Thêm banner" : "Sửa banner"}
    </h2>

    <c:if test="${not empty errorMessage}">
        <div style="background:#fff3cd;color:#856404;border:1px solid #ffc107;border-radius:8px;padding:12px 16px;margin-bottom:16px;font-size:0.95em;">
            ⚠ ${errorMessage}
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/manage/admin/banners/${empty banner.bannerId || banner.bannerId == 0 ? 'save' : 'update'}">

        <c:if test="${not empty banner.bannerId && banner.bannerId > 0}">
            <input type="hidden" name="bannerId" value="${banner.bannerId}">
        </c:if>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:16px;">
            <div>
                <label>Tên Banner <span style="color:red;">*</span></label>
                <input type="text" name="bannerName" value="${banner.bannerName}" placeholder="VD: Banner Hero 1" required style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>
            <div>
                <label>Tiêu đề chính <span style="color:red;">*</span></label>
                <input type="text" name="title" value="${banner.title}" placeholder="VD: ĐẲNG CẤP ĐỒNG HỒ CƠ" required style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>
        </div>

        <div style="margin-bottom:16px;">
            <label>Tiêu đề phụ (Subtitle)</label>
            <input type="text" name="subtitle" value="${banner.subtitle}" placeholder="VD: Giảm giá tới 20% bộ sưu tập Seiko Presage" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:16px;">
            <div>
                <label>URL Hình ảnh <span style="color:red;">*</span></label>
                <input type="text" name="imageUrl" value="${banner.imageUrl}" placeholder="VD: /assets/images/banners/banner1.jpg" required style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>
            <div>
                <label>URL Đích (Link khi click)</label>
                <input type="text" name="targetUrl" value="${banner.targetUrl}" placeholder="VD: /page/products" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:16px;margin-bottom:16px;">
            <div>
                <label>Vị trí hiển thị</label>
                <select name="positionCode" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
                    <option value="HOME_HERO" ${empty banner || banner.positionCode == 'HOME_HERO' ? 'selected' : ''}>Trang chủ (Hero Slider)</option>
                    <option value="HOME_SIDE" ${banner.positionCode == 'HOME_SIDE' ? 'selected' : ''}>Trang chủ (Cột bên)</option>
                    <option value="PRODUCT_PAGE" ${banner.positionCode == 'PRODUCT_PAGE' ? 'selected' : ''}>Trang sản phẩm</option>
                </select>
            </div>
            <div>
                <label>Thứ tự hiển thị</label>
                <input type="number" name="displayOrder" value="${empty banner ? 0 : banner.displayOrder}" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
            </div>
            <div>
                <label>Trạng thái</label>
                <select name="status" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:6px;">
                    <option value="ACTIVE" ${empty banner || banner.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE (Hiển thị)</option>
                    <option value="DRAFT" ${banner.status == 'DRAFT' ? 'selected' : ''}>DRAFT (Nháp)</option>
                    <option value="INACTIVE" ${banner.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE (Tạm ngưng)</option>
                </select>
            </div>
        </div>

        <div style="display:flex;gap:12px;margin-top:24px;">
            <button type="submit" class="button button-gold">${empty banner.bannerId || banner.bannerId == 0 ? "Thêm mới" : "Lưu thay đổi"}</button>
            <a href="${pageContext.request.contextPath}/manage/admin/banners" class="button" style="background:#eee;color:#333;text-decoration:none;padding:10px 20px;border-radius:6px;">Hủy</a>
        </div>
    </form>
</div>
