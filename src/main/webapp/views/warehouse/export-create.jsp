<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%--
  export-create.jsp — Form tạo phiếu xuất kho mới (Đầy đủ chức năng)
  POST đến /manage/warehouse/export-create
  Controller nhận: exportType, receiverName, receiverPhone, exportDate,
                   orderID, quantityNote, note
--%>

<div class="module-heading">
    <div>
        <p class="eyebrow dark">XUẤT KHO</p>
        <h2>Tạo phiếu xuất mới</h2>
        <p>Điền đầy đủ thông tin xuất kho, sau đó lưu phiếu ở trạng thái Nháp (DRAFT).</p>
    </div>
    <a class="button" href="${pageContext.request.contextPath}/manage/warehouse/exports">
        ← Quay lại danh sách
    </a>
</div>

<c:if test="${not empty sessionScope.flashMessage}">
    <div class="flash-message">${sessionScope.flashMessage}</div>
    <c:remove var="flashMessage" scope="session"/>
</c:if>

<div class="dashboard-card module-form">

    <form id="form-export-create"
          action="${pageContext.request.contextPath}/manage/warehouse/export-create"
          method="post"
          onsubmit="return validateExportForm()">

        <%-- ===== PHẦN 1: THÔNG TIN PHIẾU ===== --%>
        <p class="form-section-title">📋 Thông tin phiếu xuất</p>

        <div class="form-grid two">

            <label>
                Mã phiếu xuất
                <input type="text" value="(Tự động sinh)" readonly class="input-readonly">
            </label>

            <label>
                Ngày xuất kho <span style="color:red">*</span>
                <%-- Mặc định là hôm nay, có thể điều chỉnh --%>
                <input type="date" name="exportDate" id="exportDate" required
                       value="${pageContext.request.contextPath}">
                <script>
                    document.getElementById('exportDate').value = new Date().toISOString().split('T')[0];
                </script>
            </label>

            <label>
                Loại xuất <span style="color:red">*</span>
                <%--
                  Các giá trị phải khớp với CHECK CONSTRAINT trong CSDL:
                  ExportType IN ('SALE','TRANSFER','DAMAGED','OTHER')
                --%>
                <select name="exportType" id="exportType" required onchange="toggleOrderField()">
                    <option value="">-- Chọn loại xuất --</option>
                    <option value="SALE">📦 Xuất theo đơn hàng (SALE)</option>
                    <option value="TRANSFER">🔀 Điều chuyển kho (TRANSFER)</option>
                    <option value="DAMAGED">🔧 Hỏng / Thanh lý (DAMAGED)</option>
                    <option value="OTHER">📝 Khác (OTHER)</option>
                </select>
            </label>

            <label>
                Trạng thái khởi tạo
                <input type="text" value="Bản nháp (DRAFT) — Có thể chỉnh sửa trước khi duyệt" readonly class="input-readonly">
            </label>

        </div>

        <%-- ===== PHẦN 2: THÔNG TIN NGƯỜI NHẬN ===== --%>
        <p class="form-section-title" style="margin-top:1.5rem;">👤 Thông tin người / đơn vị nhận</p>

        <div class="form-grid two">

            <label>
                Người / Bộ phận nhận <span style="color:red">*</span>
                <input type="text" name="receiverName" id="receiverName" required
                       placeholder="Ví dụ: Nhân viên giao hàng A, Showroom Q1...">
            </label>

            <label>
                Số điện thoại người nhận
                <input type="text" name="receiverPhone" id="receiverPhone"
                       placeholder="Ví dụ: 0901234567">
            </label>

            <%-- Ô Mã đơn hàng - chỉ hiện khi loại xuất là SALE --%>
            <label id="label-orderID" style="display:none;">
                Mã đơn hàng liên kết <span style="color:red">*</span>
                <input type="number" name="orderID" id="orderID" min="1"
                       placeholder="Ví dụ: 8492 (ID đơn hàng trong hệ thống)">
            </label>

            <label id="label-transferTo" style="display:none;">
                Kho nhận (điều chuyển đến)
                <input type="text" name="transferTo" id="transferTo"
                       placeholder="Ví dụ: Kho chi nhánh Quận 1, Showroom Hà Nội...">
            </label>

        </div>

        <%-- ===== PHẦN 3: CHI TIẾT HÀNG XUẤT ===== --%>
        <p class="form-section-title" style="margin-top:1.5rem;">📦 Chi tiết hàng xuất</p>

        <div class="form-grid two">

            <label>
                Số loại mặt hàng xuất
                <input type="number" name="itemCount" id="itemCount" min="0" step="1"
                       placeholder="Ví dụ: 5 (số loại SKU xuất kho)">
            </label>

            <label>
                Tổng số lượng đơn vị xuất
                <input type="number" name="totalQty" id="totalQty" min="0" step="1"
                       placeholder="Ví dụ: 20 (tổng số chiếc xuất kho)">
            </label>

            <label class="full-field">
                Danh sách hàng / Ghi chú chi tiết
                <textarea name="note" id="note" rows="4"
                          placeholder="Ghi rõ danh sách mặt hàng xuất, SKU hoặc mô tả chi tiết lô hàng (nếu có).
Ví dụ:
- SKU-EFR-108-SL × 5 chiếc
- SKU-PRO-510-BK × 3 chiếc"></textarea>
            </label>

        </div>

        <%-- ===== PHẦN 4: XÁC NHẬN ===== --%>
        <div class="form-notice" id="form-notice" style="
            background:#fff8e1; border-left:4px solid #f9a825;
            padding:0.75rem 1rem; margin-top:1rem; border-radius:4px;
            font-size:0.875rem; color:#555; display:none;
        ">
            <b>⚠ Lưu ý:</b> Phiếu xuất theo đơn hàng sẽ cần có mã đơn hàng để liên kết.
            Hãy kiểm tra kỹ trước khi lưu.
        </div>

        <div class="form-actions">
            <a class="button"
               href="${pageContext.request.contextPath}/manage/warehouse/exports">
                Huỷ
            </a>
            <button type="submit" class="button button-gold">
                💾 Lưu phiếu xuất (DRAFT)
            </button>
        </div>

    </form>

</div>

<%-- ===== JAVASCRIPT: Ẩn/Hiện trường theo loại xuất ===== --%>
<script>
function toggleOrderField() {
    var type = document.getElementById('exportType').value;
    var labelOrder    = document.getElementById('label-orderID');
    var labelTransfer = document.getElementById('label-transferTo');
    var notice        = document.getElementById('form-notice');
    var orderInput    = document.getElementById('orderID');
    var transferInput = document.getElementById('transferTo');

    // Ẩn tất cả trước
    labelOrder.style.display    = 'none';
    labelTransfer.style.display = 'none';
    notice.style.display        = 'none';
    orderInput.required         = false;
    transferInput.required      = false;

    if (type === 'SALE') {
        labelOrder.style.display = 'block';
        orderInput.required      = true;
        notice.style.display     = 'block';
    } else if (type === 'TRANSFER') {
        labelTransfer.style.display = 'block';
    }
}

function validateExportForm() {
    var type         = document.getElementById('exportType').value;
    var receiverName = document.getElementById('receiverName').value.trim();

    if (!type) {
        alert('Vui lòng chọn loại xuất!');
        return false;
    }
    if (!receiverName) {
        alert('Vui lòng nhập tên người / bộ phận nhận!');
        return false;
    }
    if (type === 'SALE') {
        var orderId = document.getElementById('orderID').value.trim();
        if (!orderId) {
            alert('Xuất theo đơn hàng cần có mã đơn hàng liên kết!');
            document.getElementById('orderID').focus();
            return false;
        }
    }
    return true;
}
</script>
