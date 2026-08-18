<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<style>
    .voucher-form-page {
        max-width: 1000px;
        margin: 0 auto;
        padding: 32px 24px 48px;
    }

    .voucher-header {
        margin-bottom: 28px;
    }

    .voucher-header .eyebrow {
        margin: 0 0 8px;
        font-size: 12px;
        font-weight: 700;
        letter-spacing: 1.8px;
        color: #8f6814;
        text-transform: uppercase;
    }

    .voucher-header h2 {
        margin: 0;
        color: #11110f;
        font-size: 30px;
        line-height: 1.25;
        font-weight: 700;
        letter-spacing: -0.4px;
    }

    .voucher-header .module-desc {
        margin: 8px 0 0;
        color: #716e67;
        font-size: 14px;
        line-height: 1.6;
    }

    .voucher-alert {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        margin-bottom: 24px;
        padding: 14px 16px;
        border: 1px solid #f1c1c5;
        border-radius: 10px;
        background: #fff5f5;
        color: #842029;
        font-size: 14px;
        line-height: 1.5;
    }

    .voucher-alert-icon {
        flex-shrink: 0;
        font-size: 16px;
    }

    .voucher-form {
        padding: 30px;
        background: #fffdf8;
        border: 1px solid #ded8ca;
        border-radius: 14px;
        box-shadow: 0 8px 24px rgba(17, 17, 15, 0.05);
    }

    .voucher-section {
        margin-bottom: 26px;
        padding-bottom: 26px;
        border-bottom: 1px solid #e7e1d6;
    }

    .voucher-section:last-of-type {
        margin-bottom: 0;
        padding-bottom: 0;
        border-bottom: 0;
    }

    .voucher-section-title {
        margin: 0 0 18px;
        color: #11110f;
        font-size: 15px;
        font-weight: 700;
    }

    .voucher-grid {
        display: grid;
        gap: 18px;
    }

    .voucher-grid-2 {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .voucher-grid-3 {
        grid-template-columns: repeat(3, minmax(0, 1fr));
    }

    .voucher-form .form-group {
        min-width: 0;
    }

    .voucher-form label {
        display: block;
        margin-bottom: 7px;
        color: #292823;
        font-size: 13px;
        font-weight: 650;
        line-height: 1.4;
    }

    .voucher-form label .required {
        color: #b42318;
    }

    .voucher-form input,
    .voucher-form select,
    .voucher-form textarea {
        box-sizing: border-box;
        width: 100%;
        border: 1px solid #d8d1c4;
        border-radius: 8px;
        background: #ffffff;
        color: #171713;
        font-family: inherit;
        font-size: 14px;
        outline: none;
        transition:
            border-color 0.18s ease,
            box-shadow 0.18s ease,
            background-color 0.18s ease;
    }

    .voucher-form input,
    .voucher-form select {
        height: 44px;
        padding: 0 13px;
    }

    .voucher-form textarea {
        min-height: 100px;
        padding: 12px 13px;
        resize: vertical;
        line-height: 1.55;
    }

    .voucher-form input::placeholder,
    .voucher-form textarea::placeholder {
        color: #aaa49a;
    }

    .voucher-form input:hover,
    .voucher-form select:hover,
    .voucher-form textarea:hover {
        border-color: #b9b1a3;
    }

    .voucher-form input:focus,
    .voucher-form select:focus,
    .voucher-form textarea:focus {
        border-color: #b58a27;
        box-shadow: 0 0 0 3px rgba(181, 138, 39, 0.12);
        background: #fffefa;
    }

    .voucher-form input[name="voucherCode"] {
        text-transform: uppercase;
        font-weight: 650;
        letter-spacing: 0.5px;
    }

    .voucher-form input[type="number"] {
        appearance: textfield;
    }

    .voucher-form input[type="number"]::-webkit-outer-spin-button,
    .voucher-form input[type="number"]::-webkit-inner-spin-button {
        margin: 0;
    }

    .voucher-help {
        margin-top: 6px;
        color: #858077;
        font-size: 12px;
        line-height: 1.45;
    }

    .voucher-actions {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-top: 28px;
        padding-top: 24px;
        border-top: 1px solid #e7e1d6;
    }

    .voucher-actions .button {
        min-height: 42px;
        box-sizing: border-box;
        border-radius: 8px;
        padding: 0 20px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-family: inherit;
        font-size: 14px;
        font-weight: 650;
        text-decoration: none;
        cursor: pointer;
        transition:
            transform 0.15s ease,
            box-shadow 0.15s ease,
            background-color 0.15s ease,
            border-color 0.15s ease;
    }

    .voucher-actions .button:hover {
        transform: translateY(-1px);
    }

    .voucher-actions .button-gold {
        border: 1px solid #9f761d;
        background: #b58a27;
        color: #ffffff;
        box-shadow: 0 4px 10px rgba(143, 104, 20, 0.18);
    }

    .voucher-actions .button-gold:hover {
        background: #8f6814;
        box-shadow: 0 6px 14px rgba(143, 104, 20, 0.22);
    }

    .voucher-actions .button-secondary {
        border: 1px solid #d2cabc;
        background: #ffffff;
        color: #403d36;
    }

    .voucher-actions .button-secondary:hover {
        border-color: #bdb5a7;
        background: #f8f5ee;
    }

    @media (max-width: 850px) {
        .voucher-grid-3 {
            grid-template-columns: repeat(2, minmax(0, 1fr));
        }
    }

    @media (max-width: 650px) {
        .voucher-form-page {
            padding: 22px 16px 36px;
        }

        .voucher-form {
            padding: 20px;
            border-radius: 11px;
        }

        .voucher-header h2 {
            font-size: 25px;
        }

        .voucher-grid-2,
        .voucher-grid-3 {
            grid-template-columns: 1fr;
        }

        .voucher-actions {
            flex-direction: column;
            align-items: stretch;
        }

        .voucher-actions .button {
            width: 100%;
        }
    }
</style>

<div class="voucher-form-page">

    <div class="voucher-header">
        <p class="eyebrow">VOUCHER &amp; KHUYẾN MÃI</p>

        <h2>
            ${empty voucher.voucherId ? "Thêm Voucher Mới" : "Chỉnh Sửa Voucher"}
        </h2>

        <p class="module-desc">
            Tạo mới hoặc cập nhật mã giảm giá cho hệ thống WatchStore.
        </p>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="voucher-alert">
            <span class="voucher-alert-icon">⚠️</span>
            <span>${errorMessage}</span>
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/manage/admin/vouchers"
          class="voucher-form">

        <c:if test="${not empty voucher.voucherId}">
            <input type="hidden"
                   name="id"
                   value="${voucher.voucherId}">
        </c:if>

        <!-- THÔNG TIN VOUCHER -->
        <div class="voucher-section">

            <h3 class="voucher-section-title">
                Thông tin voucher
            </h3>

            <div class="voucher-grid voucher-grid-2">

                <div class="form-group">
                    <label>
                        Mã Voucher
                        <span class="required">*</span>
                    </label>

                    <input type="text"
                           name="voucherCode"
                           value="${voucher.voucherCode}"
                           placeholder="VD: WELCOME100, SUMMER2026"
                           required>
                </div>

                <div class="form-group">
                    <label>
                        Tên Voucher
                        <span class="required">*</span>
                    </label>

                    <input type="text"
                           name="voucherName"
                           value="${voucher.voucherName}"
                           placeholder="VD: Giảm 100K Cho Đơn Hàng Đầu Tiên"
                           required>
                </div>

            </div>
        </div>

        <!-- GIẢM GIÁ -->
        <div class="voucher-section">

            <h3 class="voucher-section-title">
                Cấu hình giảm giá
            </h3>

            <div class="voucher-grid voucher-grid-3">

                <div class="form-group">
                    <label>
                        Loại giảm giá
                        <span class="required">*</span>
                    </label>

                    <select name="discountType">
                        <option value="PERCENT"
                                ${voucher.discountType == 'PERCENT' ? 'selected' : ''}>
                            Giảm theo % (PERCENT)
                        </option>

                        <option value="FIXED"
                                ${voucher.discountType == 'FIXED' ? 'selected' : ''}>
                            Giảm tiền cố định (FIXED)
                        </option>

                        <option value="FREESHIP"
                                ${voucher.discountType == 'FREESHIP' ? 'selected' : ''}>
                            Miễn phí vận chuyển (FREESHIP)
                        </option>
                    </select>
                </div>

                <div class="form-group">
                    <label>
                        Giá trị giảm
                        <span class="required">*</span>
                    </label>

                    <input type="number"
                           step="any"
                           min="0"
                           name="discountValue"
                           value="${voucher.discountValue}"
                           placeholder="VD: 10 (%) hoặc 100000 (₫)"
                           required>
                </div>

                <div class="form-group">
                    <label>
                        Giảm tối đa (₫)
                    </label>

                    <input type="number"
                           step="any"
                           min="0"
                           name="maximumDiscount"
                           value="${voucher.maximumDiscount}"
                           placeholder="VD: 1000000 (Để trống nếu không giới hạn)">
                </div>

            </div>
        </div>

        <!-- ĐIỀU KIỆN SỬ DỤNG -->
        <div class="voucher-section">

            <h3 class="voucher-section-title">
                Điều kiện sử dụng
            </h3>

            <div class="voucher-grid voucher-grid-3">

                <div class="form-group">
                    <label>
                        Đơn hàng tối thiểu (₫)
                    </label>

                    <input type="number"
                           step="any"
                           min="0"
                           name="minimumOrderValue"
                           value="${empty voucher.minimumOrderValue ? 0 : voucher.minimumOrderValue}"
                           placeholder="VD: 2000000">
                </div>

                <div class="form-group">
                    <label>
                        Tổng số lượt dùng
                    </label>

                    <input type="number"
                           min="1"
                           name="usageLimit"
                           value="${voucher.usageLimit}"
                           placeholder="VD: 100 (Để trống = Không giới hạn)">
                </div>

                <div class="form-group">
                    <label>
                        Số lượt dùng / Khách
                    </label>

                    <input type="number"
                           min="1"
                           name="usageLimitPerUser"
                           value="${empty voucher.usageLimitPerUser ? 1 : voucher.usageLimitPerUser}"
                           required>
                </div>

            </div>
        </div>

        <!-- THỜI GIAN -->
        <div class="voucher-section">

            <h3 class="voucher-section-title">
                Thời gian áp dụng
            </h3>

            <div class="voucher-grid voucher-grid-2">

                <div class="form-group">
                    <label>
                        Thời gian bắt đầu
                        <span class="required">*</span>
                    </label>

                    <input type="datetime-local"
                           name="startAt"
                           value="${startAtFormatted}"
                           min="${minDateTimeFormatted}"
                           required>
                </div>

                <div class="form-group">
                    <label>
                        Thời gian kết thúc
                        <span class="required">*</span>
                    </label>

                    <input type="datetime-local"
                           name="endAt"
                           value="${endAtFormatted}"
                           min="${minDateTimeFormatted}"
                           required>
                </div>

            </div>
        </div>

        <!-- TRẠNG THÁI -->
        <div class="voucher-section">

            <h3 class="voucher-section-title">
                Hiển thị &amp; trạng thái
            </h3>

            <div class="voucher-grid voucher-grid-2">

                <div class="form-group">
                    <label>
                        Quyền riêng tư
                    </label>

                    <select name="isPublic">
                        <option value="true"
                                ${empty voucher || voucher.isPublic ? 'selected' : ''}>
                            Công khai (Hiện trên website)
                        </option>

                        <option value="false"
                                ${voucher != null && !voucher.isPublic ? 'selected' : ''}>
                            Riêng tư (Chỉ dành cho tài khoản được gán)
                        </option>
                    </select>
                </div>

                <div class="form-group">
                    <label>
                        Trạng thái
                    </label>

                    <select name="status">
                        <option value="ACTIVE"
                                ${empty voucher || voucher.status == 'ACTIVE' ? 'selected' : ''}>
                            ACTIVE (Hoạt động)
                        </option>

                        <option value="INACTIVE"
                                ${voucher.status == 'INACTIVE' ? 'selected' : ''}>
                            INACTIVE (Ngừng hoạt động)
                        </option>

                        <option value="DRAFT"
                                ${voucher.status == 'DRAFT' ? 'selected' : ''}>
                            DRAFT (Nháp)
                        </option>

                        <option value="EXPIRED"
                                ${voucher.status == 'EXPIRED' ? 'selected' : ''}>
                            EXPIRED (Hết hạn)
                        </option>
                    </select>
                </div>

            </div>
        </div>

        <!-- MÔ TẢ -->
        <div class="voucher-section">

            <h3 class="voucher-section-title">
                Mô tả &amp; ghi chú
            </h3>

            <div class="form-group">

                <label>
                    Mô tả / Ghi chú
                </label>

                <textarea name="description"
                          rows="3"
                          placeholder="Nhập mô tả chương trình ưu đãi...">${voucher.description}</textarea>

            </div>

        </div>

        <!-- ACTION -->
        <div class="voucher-actions">

            <button type="submit"
                    class="button button-gold">
                ${empty voucher.voucherId ? "Thêm Voucher" : "Lưu Thay Đổi"}
            </button>

            <a class="button button-secondary"
               href="${pageContext.request.contextPath}/manage/admin/vouchers">
                Quay lại
            </a>

        </div>

    </form>
</div>