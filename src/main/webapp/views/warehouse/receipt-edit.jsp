<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%--
  receipt-edit.jsp — Form chỉnh sửa phiếu nhập kho
--%>

<div class="module-heading">
    <div>
        <p class="eyebrow dark">NHẬP KHO</p>
        <h2>Chỉnh sửa phiếu nhập #${receipt.receiptCode}</h2>
        <p>Cập nhật thông tin nhà cung cấp, giá trị tiền, ghi chú hoặc trạng thái phiếu nhập.</p>
    </div>
    <a class="button" href="${pageContext.request.contextPath}/manage/warehouse/receipts">
        ← Quay lại danh sách
    </a>
</div>

<div class="dashboard-card module-form">

    <form action="${pageContext.request.contextPath}/manage/warehouse/receipt-edit" method="post">
        
        <input type="hidden" name="id" value="${receipt.receiptID}" />

        <div class="form-grid two">

            <label>
                Mã phiếu nhập
                <input type="text" value="${receipt.receiptCode}" readonly />
            </label>

            <label>
                Ngày tạo
                <input type="text" value="${receipt.receiptDate}" readonly />
            </label>

            <label>
                Tên nhà cung cấp <span style="color:red">*</span>
                <input type="text" name="supplierName" value="${receipt.supplierName}" required />
            </label>

            <label>
                Số điện thoại nhà cung cấp
                <input type="text" name="supplierPhone" value="${receipt.supplierPhone}" />
            </label>

            <label>
                Tổng giá trị hàng nhập (VNĐ) <span style="color:red">*</span>
                <input type="number" name="totalCost" value="${receipt.totalCost}" required min="0" step="1000" />
            </label>

            <label>
                Trạng thái phiếu
                <select name="status">
                    <option value="DRAFT" ${receipt.status == 'DRAFT' ? 'selected' : ''}>Bản nháp (DRAFT)</option>
                    <option value="PENDING" ${receipt.status == 'PENDING' ? 'selected' : ''}>Chờ duyệt (PENDING)</option>
                    <option value="COMPLETED" ${receipt.status == 'COMPLETED' ? 'selected' : ''}>Hoàn thành - Chốt kho (COMPLETED)</option>
                    <option value="CANCELLED" ${receipt.status == 'CANCELLED' ? 'selected' : ''}>Đã huỷ (CANCELLED)</option>
                </select>
            </label>

            <label class="full-field">
                Ghi chú
                <textarea name="note" rows="3">${receipt.note}</textarea>
            </label>

        </div>

        <div class="form-actions">
            <a class="button" href="${pageContext.request.contextPath}/manage/warehouse/receipts">
                Huỷ
            </a>
            <button type="submit" class="button button-gold">
                Lưu thay đổi
            </button>
        </div>

    </form>

</div>
