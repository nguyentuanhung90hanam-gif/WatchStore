<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%--
  export-edit.jsp — Form chỉnh sửa phiếu xuất kho
--%>

<div class="module-heading">
    <div>
        <p class="eyebrow dark">XUẤT KHO</p>
        <h2>Chỉnh sửa phiếu xuất #${export.exportCode}</h2>
        <p>Cập nhật loại xuất, người nhận, trạng thái hoặc ghi chú.</p>
    </div>
    <a class="button" href="${pageContext.request.contextPath}/manage/warehouse/exports">
        ← Quay lại danh sách
    </a>
</div>

<div class="dashboard-card module-form">

    <form action="${pageContext.request.contextPath}/manage/warehouse/export-edit" method="post">

        <input type="hidden" name="id" value="${export.exportID}" />

        <div class="form-grid two">

            <label>
                Mã phiếu xuất
                <input type="text" value="${export.exportCode}" readonly />
            </label>

            <label>
                Loại xuất <span style="color:red">*</span>
                <select name="exportType" required>
                    <option value="SALE" ${export.exportType == 'SALE' ? 'selected' : ''}>Xuất theo đơn hàng</option>
                    <option value="TRANSFER" ${export.exportType == 'TRANSFER' ? 'selected' : ''}>Điều chuyển kho</option>
                    <option value="DAMAGED" ${export.exportType == 'DAMAGED' ? 'selected' : ''}>Hỏng / Thanh lý</option>
                    <option value="OTHER" ${export.exportType == 'OTHER' ? 'selected' : ''}>Khác</option>
                </select>
            </label>

            <label>
                Người / Bộ phận nhận
                <input type="text" name="receiverName" value="${export.receiverName}" />
            </label>

            <label>
                Trạng thái phiếu
                <select name="status">
                    <option value="DRAFT" ${export.status == 'DRAFT' ? 'selected' : ''}>Nháp (DRAFT)</option>
                    <option value="PENDING" ${export.status == 'PENDING' ? 'selected' : ''}>Chờ duyệt (PENDING)</option>
                    <option value="COMPLETED" ${export.status == 'COMPLETED' ? 'selected' : ''}>Hoàn thành (COMPLETED)</option>
                    <option value="CANCELLED" ${export.status == 'CANCELLED' ? 'selected' : ''}>Đã huỷ (CANCELLED)</option>
                </select>
            </label>

            <label class="full-field">
                Ghi chú
                <textarea name="note" rows="3">${export.note}</textarea>
            </label>

        </div>

        <div class="form-actions">
            <a class="button" href="${pageContext.request.contextPath}/manage/warehouse/exports">
                Huỷ
            </a>
            <button type="submit" class="button button-gold">
                Lưu thay đổi
            </button>
        </div>

    </form>

</div>
