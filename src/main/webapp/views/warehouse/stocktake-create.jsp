<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">KIỂM KÊ KHO</p>
        <h2>Tạo Phiếu Kiểm Kê</h2>
        <p class="module-desc">Nhập số lượng thực tế để cân bằng tồn kho hệ thống.</p>
    </div>
</div>

<div class="dashboard-card" style="max-width: 900px; margin: 0 auto;">
    <form action="${cp}/manage/warehouse/stocktake-create" method="POST">
        <div class="form-grid" style="display:grid; grid-template-columns: 1fr 1fr; gap:20px; margin-bottom: 24px;">
            <div class="form-group">
                <label>Kho hàng *</label>
                <select name="warehouseId" class="input-field" required>
                    <c:forEach items="${warehouses}" var="w">
                        <option value="${w.warehouseId}">${w.warehouseName}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group">
                <label>Ghi chú / Lý do kiểm kê</label>
                <input type="text" name="note" class="input-field" placeholder="Ghi chú phiếu kiểm kê">
            </div>
        </div>

        <h3 style="margin-bottom: 12px; font-size: 16px;">Sản phẩm kiểm kê</h3>
        <table class="item-table" style="width: 100%; margin-bottom: 16px;">
            <thead>
                <tr>
                    <th style="width: 50%;">Sản phẩm (Biến thể)</th>
                    <th style="width: 30%;">Số lượng thực tế (Actual)</th>
                    <th style="width: 20%;">Thao tác</th>
                </tr>
            </thead>
            <tbody id="itemList">
                <tr class="item-row">
                    <td>
                        <select name="variantIds" class="input-field" required>
                            <option value="">-- Chọn sản phẩm --</option>
                            <c:forEach items="${variants}" var="v">
                                <option value="${v.variantId}">${v.sku} - ${v.productName} (${v.variantName})</option>
                            </c:forEach>
                        </select>
                    </td>
                    <td>
                        <input type="number" name="actualQuantities" class="input-field" value="0" min="0" required>
                    </td>
                    <td>
                        <button type="button" class="button button-outline" onclick="removeRow(this)" style="color:red; border-color:red;">Xóa</button>
                    </td>
                </tr>
            </tbody>
        </table>

        <button type="button" class="button button-outline" onclick="addRow()" style="margin-bottom: 24px;">+ Thêm dòng</button>

        <div style="display:flex; justify-content:flex-end; gap:12px; border-top:1px solid #eee; padding-top:20px;">
            <a href="${cp}/manage/warehouse/stocktake" class="button button-outline">Hủy bỏ</a>
            <button type="submit" class="button button-gold">Lưu Phiếu Nháp</button>
        </div>
    </form>
</div>

<script>
function addRow() {
    const tbody = document.getElementById('itemList');
    const firstRow = tbody.querySelector('.item-row');
    const newRow = firstRow.cloneNode(true);
    newRow.querySelector('select').value = '';
    newRow.querySelector('input').value = '0';
    tbody.appendChild(newRow);
}

function removeRow(btn) {
    const tbody = document.getElementById('itemList');
    if (tbody.querySelectorAll('.item-row').length > 1) {
        btn.closest('tr').remove();
    } else {
        alert('Phải có ít nhất 1 dòng!');
    }
}
</script>
