<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- variant-create.jsp — Form thêm biến thể mới --%>

<div class="module-heading">
    <div>
        <p class="eyebrow dark">BIẾN THỂ</p>
        <h2>Thêm biến thể mới</h2>
        <p>Điền thông tin SKU, màu sắc, chất liệu và giá bán cho biến thể sản phẩm.</p>
    </div>
    <a href="${pageContext.request.contextPath}/manage/warehouse/variants"
       class="button button-outline">← Quay lại danh sách</a>
</div>

<div class="dashboard-card module-form">
    <form action="${pageContext.request.contextPath}/manage/warehouse/variant-create"
          method="post">

        <div class="form-grid two">

            <%-- Mã SKU --%>
            <label>
                Mã SKU <span style="color:red">*</span>
                <input type="text" name="sku" placeholder="VD: SKU-EFR-108-SL" required>
                <small>Mã định danh duy nhất cho biến thể này.</small>
            </label>

            <%-- Tên biến thể --%>
            <label>
                Tên biến thể <span style="color:red">*</span>
                <input type="text" name="variantName" placeholder="VD: Mặt Xanh / Dây thép" required>
            </label>

            <%-- Màu sắc --%>
            <label>
                Màu sắc
                <input type="text" name="color" placeholder="VD: Xanh, Đen, Vàng...">
            </label>

            <%-- Chất liệu dây --%>
            <label>
                Chất liệu dây
                <input type="text" name="material" placeholder="VD: Thép không gỉ, Da, Cao su...">
            </label>

            <%-- Giá bán --%>
            <label>
                Giá bán (VNĐ) <span style="color:red">*</span>
                <input type="number" name="price" min="0" step="1000" placeholder="VD: 4500000" required>
            </label>

            <%-- ID sản phẩm cha --%>
            <label>
                ID Sản phẩm cha <span style="color:red">*</span>
                <input type="number" name="productId" min="1" placeholder="VD: 1" required>
                <small>Nhập ID của sản phẩm chứa biến thể này.</small>
            </label>

        </div>

        <div class="form-actions">
            <button type="submit" class="button button-gold">✅ Lưu biến thể</button>
            <a href="${pageContext.request.contextPath}/manage/warehouse/variants"
               class="button button-outline">Hủy</a>
        </div>

    </form>
</div>
