<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- variant-edit.jsp — Form sửa biến thể --%>

<div class="module-heading">
    <div>
        <p class="eyebrow dark">BIẾN THỂ</p>
        <h2>Sửa biến thể: ${variant.sku}</h2>
        <p>Cập nhật thông tin biến thể. SKU không thể thay đổi sau khi tạo.</p>
    </div>
    <a href="${pageContext.request.contextPath}/manage/warehouse/variants"
       class="button button-outline">← Quay lại danh sách</a>
</div>

<div class="dashboard-card module-form">
    <form action="${pageContext.request.contextPath}/manage/warehouse/variant-edit"
          method="post">

        <%-- Truyền ID ẩn để controller biết cần update bản ghi nào --%>
        <input type="hidden" name="id" value="${variant.variantID}">

        <div class="form-grid two">

            <%-- SKU chỉ đọc, không được sửa --%>
            <label>
                Mã SKU
                <input type="text" value="${variant.sku}" disabled>
                <small>SKU không thể thay đổi sau khi đã tạo.</small>
            </label>

            <%-- Tên biến thể --%>
            <label>
                Tên biến thể <span style="color:red">*</span>
                <input type="text" name="variantName" value="${variant.variantName}" required>
            </label>

            <%-- Màu sắc --%>
            <label>
                Màu sắc
                <input type="text" name="color" value="${variant.color}">
            </label>

            <%-- Chất liệu dây --%>
            <label>
                Chất liệu dây
                <input type="text" name="material" value="${variant.material}">
            </label>

            <%-- Giá bán --%>
            <label>
                Giá bán (VNĐ)
                <input type="number" name="price" min="0" step="1000" value="${variant.price}">
            </label>

            <%-- Trạng thái --%>
            <label>
                Trạng thái
                <select name="status">
                    <option value="ACTIVE"   ${variant.status == 'ACTIVE'   ? 'selected' : ''}>Đang bán</option>
                    <option value="INACTIVE" ${variant.status == 'INACTIVE' ? 'selected' : ''}>Ngừng bán</option>
                </select>
            </label>

            <%-- Thông tin chỉ đọc --%>
            <label>
                Thuộc sản phẩm
                <input type="text" value="${variant.productCode} — ${variant.productName}" disabled>
            </label>

            <label>
                Tồn kho hiện tại
                <input type="text" value="${variant.stockQty} sản phẩm" disabled>
                <small>Tồn kho được quản lý qua phiếu nhập/xuất kho.</small>
            </label>

        </div>

        <div class="form-actions">
            <button type="submit" class="button button-gold">💾 Lưu thay đổi</button>
            <a href="${pageContext.request.contextPath}/manage/warehouse/variants"
               class="button button-outline">Hủy</a>
        </div>

    </form>
</div>
