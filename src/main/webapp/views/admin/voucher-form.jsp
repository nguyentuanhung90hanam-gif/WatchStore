<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="form-container" style="max-width: 900px; margin: 0 auto; padding: 24px;">
    <div style="margin-bottom: 24px;">
        <p class="eyebrow dark">VOUCHER & KHUYẾN MÃI</p>
        <h2>${empty voucher.voucherId ? "Thêm Voucher Mới" : "Chỉnh Sửa Voucher"}</h2>
        <p class="module-desc">Tạo mới hoặc cập nhật mã giảm giá cho hệ thống WatchStore.</p>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger" style="background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; padding: 14px 18px; border-radius: 8px; margin-bottom: 24px; font-weight: 500;">
            ⚠️ ${errorMessage}
        </div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/manage/admin/vouchers" class="portal-form">
        <c:if test="${not empty voucher.voucherId}">
            <input type="hidden" name="id" value="${voucher.voucherId}">
        </c:if>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
            <div class="form-group">
                <label style="font-weight: 600; margin-bottom: 6px; display: block;">Mã Voucher <span style="color: red;">*</span></label>
                <input type="text"
                       name="voucherCode"
                       value="${voucher.voucherCode}"
                       placeholder="VD: WELCOME100, SUMMER2026"
                       style="text-transform: uppercase;"
                       required>
            </div>

            <div class="form-group">
                <label style="font-weight: 600; margin-bottom: 6px; display: block;">Tên Voucher <span style="color: red;">*</span></label>
                <input type="text"
                       name="voucherName"
                       value="${voucher.voucherName}"
                       placeholder="VD: Giảm 100K Cho Đơn Hàng Đầu Tiên"
                       required>
            </div>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 20px; margin-top: 16px;">
            <div class="form-group">
                <label style="font-weight: 600; margin-bottom: 6px; display: block;">Loại giảm giá <span style="color: red;">*</span></label>
                <select name="discountType">
                    <option value="PERCENT" ${voucher.discountType == 'PERCENT' ? 'selected' : ''}>Giảm theo % (PERCENT)</option>
                    <option value="FIXED" ${voucher.discountType == 'FIXED' ? 'selected' : ''}>Giảm tiền cố định (FIXED)</option>
                    <option value="FREESHIP" ${voucher.discountType == 'FREESHIP' ? 'selected' : ''}>Miễn phí vận chuyển (FREESHIP)</option>
                </select>
            </div>

            <div class="form-group">
                <label style="font-weight: 600; margin-bottom: 6px; display: block;">Giá trị giảm <span style="color: red;">*</span></label>
                <input type="number"
                       step="any"
                       min="0"
                       name="discountValue"
                       value="${voucher.discountValue}"
                       placeholder="VD: 10 (%) hoặc 100000 (₫)"
                       required>
            </div>

            <div class="form-group">
                <label style="font-weight: 600; margin-bottom: 6px; display: block;">Giảm tối đa (₫)</label>
                <input type="number"
                       step="any"
                       min="0"
                       name="maximumDiscount"
                       value="${voucher.maximumDiscount}"
                       placeholder="VD: 1000000 (Để trống nếu không giới hạn)">
            </div>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 20px; margin-top: 16px;">
            <div class="form-group">
                <label style="font-weight: 600; margin-bottom: 6px; display: block;">Đơn hàng tối thiểu (₫)</label>
                <input type="number"
                       step="any"
                       min="0"
                       name="minimumOrderValue"
                       value="${empty voucher.minimumOrderValue ? 0 : voucher.minimumOrderValue}"
                       placeholder="VD: 2000000">
            </div>

            <div class="form-group">
                <label style="font-weight: 600; margin-bottom: 6px; display: block;">Tổng số lượt dùng</label>
                <input type="number"
                       min="1"
                       name="usageLimit"
                       value="${voucher.usageLimit}"
                       placeholder="VD: 100 (Để trống = Không giới hạn)">
            </div>

            <div class="form-group">
                <label style="font-weight: 600; margin-bottom: 6px; display: block;">Số lượt dùng / Khách</label>
                <input type="number"
                       min="1"
                       name="usageLimitPerUser"
                       value="${empty voucher.usageLimitPerUser ? 1 : voucher.usageLimitPerUser}"
                       required>
            </div>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-top: 16px;">
            <div class="form-group">
                <label style="font-weight: 600; margin-bottom: 6px; display: block;">Thời gian bắt đầu <span style="color: red;">*</span></label>
                <input type="datetime-local"
                       name="startAt"
                       value="${startAtFormatted}"
                       required>
            </div>

            <div class="form-group">
                <label style="font-weight: 600; margin-bottom: 6px; display: block;">Thời gian kết thúc <span style="color: red;">*</span></label>
                <input type="datetime-local"
                       name="endAt"
                       value="${endAtFormatted}"
                       required>
            </div>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-top: 16px;">
            <div class="form-group">
                <label style="font-weight: 600; margin-bottom: 6px; display: block;">Quyền riêng tư</label>
                <select name="isPublic">
                    <option value="true" ${empty voucher || voucher.isPublic ? 'selected' : ''}>Công khai (Hiện trên website)</option>
                    <option value="false" ${voucher != null && !voucher.isPublic ? 'selected' : ''}>Riêng tư (Chỉ dành cho tài khoản được gán)</option>
                </select>
            </div>

            <div class="form-group">
                <label style="font-weight: 600; margin-bottom: 6px; display: block;">Trạng thái</label>
                <select name="status">
                    <option value="ACTIVE" ${empty voucher || voucher.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE (Hoạt động)</option>
                    <option value="INACTIVE" ${voucher.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE (Ngừng hoạt động)</option>
                    <option value="DRAFT" ${voucher.status == 'DRAFT' ? 'selected' : ''}>DRAFT (Nháp)</option>
                    <option value="EXPIRED" ${voucher.status == 'EXPIRED' ? 'selected' : ''}>EXPIRED (Hết hạn)</option>
                </select>
            </div>
        </div>

        <div class="form-group" style="margin-top: 16px;">
            <label style="font-weight: 600; margin-bottom: 6px; display: block;">Mô tả / Ghi chú</label>
            <textarea name="description" rows="3" placeholder="Nhập mô tả chương trình ưu đãi...">${voucher.description}</textarea>
        </div>

        <div style="margin-top: 28px; display: flex; gap: 12px; align-items: center;">
            <button type="submit" class="button button-gold">
                ${empty voucher.voucherId ? "Thêm Voucher" : "Lưu Thay Đổi"}
            </button>

            <a class="button button-secondary"
               href="${pageContext.request.contextPath}/manage/admin/vouchers"
               style="text-decoration: none; padding: 10px 20px; border-radius: 6px;">
                Quay lại
            </a>
        </div>
    </form>
</div>