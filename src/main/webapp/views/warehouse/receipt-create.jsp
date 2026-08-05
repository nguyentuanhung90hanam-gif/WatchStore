<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%--
  receipt-create.jsp — Form tạo phiếu nhập kho mới (Đầy đủ chức năng)
  Form bố trí nhiều cột:
  - Mã phiếu (tự động) | Ngày nhập (chọn được)
  - Tên nhà cung cấp (*) | Số điện thoại nhà cung cấp
  - Email nhà cung cấp | Loại hàng nhập
  - Tổng giá trị nhập (*) | Trạng thái mặc định (DRAFT)
  - Số lượng mặt hàng | Đơn vị tính
  - Dòng dưới: Ghi chú chi tiết
--%>

<div class="module-heading">
    <div>
        <p class="eyebrow dark">NHẬP KHO</p>
        <h2>Tạo phiếu nhập mới</h2>
        <p>Điền thông tin nhà cung cấp, giá trị hàng hóa và ghi chú, sau đó xác nhận để lưu phiếu.</p>
    </div>
    <a class="button" href="${pageContext.request.contextPath}/manage/warehouse/receipts">
        ← Quay lại danh sách
    </a>
</div>

<c:if test="${not empty sessionScope.flashMessage}">
    <div class="flash-message">${sessionScope.flashMessage}</div>
    <c:remove var="flashMessage" scope="session"/>
</c:if>

<div class="dashboard-card module-form">

    <form id="form-receipt-create"
          action="${pageContext.request.contextPath}/manage/warehouse/receipt-create"
          method="post"
          onsubmit="return validateReceiptForm()">

        <%-- ===== PHẦN 1: THÔNG TIN PHIẾU ===== --%>
        <p class="form-section-title">📋 Thông tin phiếu nhập</p>

        <div class="form-grid two">

            <%-- Dòng 1: Mã phiếu & Ngày nhập --%>
            <label>
                Mã phiếu nhập
                <input type="text" value="(Hệ thống tự động sinh)" readonly class="input-readonly"/>
            </label>

            <label>
                Ngày nhập kho <span style="color:red">*</span>
                <input type="date" name="receiptDate" id="receiptDate" required />
                <script>
                    document.getElementById('receiptDate').value = new Date().toISOString().split('T')[0];
                </script>
            </label>

            <%-- Trạng thái & Loại hàng --%>
            <label>
                Trạng thái khởi tạo
                <input type="text" value="Bản nháp (DRAFT) — Có thể sửa/xóa trước khi duyệt" readonly class="input-readonly"/>
            </label>

            <label>
                Loại hàng nhập <span style="color:red">*</span>
                <select name="receiptType" id="receiptType" required>
                    <option value="NEW">🆕 Hàng mới nhập</option>
                    <option value="RESTOCK">🔄 Nhập bổ sung tồn kho</option>
                    <option value="RETURN">↩ Hàng trả về từ khách</option>
                    <option value="TRANSFER">🔀 Nhận điều chuyển từ kho khác</option>
                    <option value="OTHER">📝 Khác</option>
                </select>
            </label>

        </div>

        <%-- ===== PHẦN 2: THÔNG TIN NHÀ CUNG CẤP ===== --%>
        <p class="form-section-title" style="margin-top:1.5rem;">🏭 Thông tin nhà cung cấp</p>

        <div class="form-grid two">

            <%-- Dòng 2: Nhà cung cấp & SĐT --%>
            <label>
                Tên nhà cung cấp <span style="color:red">*</span>
                <input type="text" name="supplierName" required
                       placeholder="Ví dụ: Công ty Seiko Việt Nam"/>
            </label>

            <label>
                Số điện thoại nhà cung cấp
                <input type="text" name="supplierPhone"
                       placeholder="Ví dụ: 0901234567"/>
            </label>

            <label>
                Email nhà cung cấp
                <input type="email" name="supplierEmail"
                       placeholder="Ví dụ: contact@seiko.vn"/>
            </label>

            <label>
                Địa chỉ nhà cung cấp
                <input type="text" name="supplierAddress"
                       placeholder="Ví dụ: 123 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM"/>
            </label>

        </div>

        <%-- ===== PHẦN 3: THÔNG TIN HÀNG HÓA ===== --%>
        <p class="form-section-title" style="margin-top:1.5rem;">📦 Thông tin hàng hóa</p>

        <div class="form-grid two">

            <%-- Dòng 3: Tổng tiền nhập & Số lượng --%>
            <label>
                Tổng giá trị hàng nhập (VNĐ) <span style="color:red">*</span>
                <input type="number" name="totalCost" required min="0" step="1000"
                       placeholder="Ví dụ: 45000000"
                       oninput="formatCurrency(this)"/>
            </label>

            <label>
                Số lượng mặt hàng (loại SKU)
                <input type="number" name="itemCount" min="1" step="1"
                       placeholder="Ví dụ: 10 (số loại sản phẩm khác nhau)"/>
            </label>

            <label>
                Tổng số lượng đơn vị nhập
                <input type="number" name="totalQty" min="1" step="1"
                       placeholder="Ví dụ: 50 (tổng số chiếc nhập kho)"/>
            </label>

            <label>
                Số hợp đồng / Số hóa đơn
                <input type="text" name="invoiceNo"
                       placeholder="Ví dụ: INV-2026-08-001"/>
            </label>

            <%-- Dòng 4: Ghi chú đầy đủ chiều ngang --%>
            <label class="full-field">
                Ghi chú phiếu nhập
                <textarea name="note" rows="4"
                          placeholder="Ghi chú chi tiết về lô hàng nhập, danh sách sản phẩm, điều kiện đặc biệt (nếu có).
Ví dụ:
- Seiko Prospex SRPE03 × 10 chiếc
- Casio G-Shock GA-2100 × 20 chiếc"></textarea>
            </label>

        </div>

        <div class="form-actions">
            <a class="button" href="${pageContext.request.contextPath}/manage/warehouse/receipts">
                Huỷ
            </a>
            <button type="submit" class="button button-gold">
                💾 Lưu phiếu nhập (DRAFT)
            </button>
        </div>

    </form>

</div>

<script>
function validateReceiptForm() {
    var supplierName = document.querySelector('[name="supplierName"]').value.trim();
    var totalCost    = document.querySelector('[name="totalCost"]').value.trim();

    if (!supplierName) {
        alert('Vui lòng nhập tên nhà cung cấp!');
        return false;
    }
    if (!totalCost || parseFloat(totalCost) < 0) {
        alert('Vui lòng nhập tổng giá trị hàng nhập hợp lệ!');
        return false;
    }
    return true;
}
</script>
