<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">NHẬP KHO</p>
        <h2>Tạo phiếu nhập mới</h2>
        <p class="module-desc">Phiếu sẽ được lưu ở trạng thái DRAFT để kiểm tra trước khi gửi duyệt.</p>
    </div>
    <a class="button button-outline" href="${cp}/manage/warehouse/receipts">Quay lại</a>
</div>

<c:if test="${not empty sessionScope.errorMsg}">
    <div class="alert alert-error">${sessionScope.errorMsg}</div>
    <c:remove var="errorMsg" scope="session"/>
</c:if>

<div class="dashboard-card module-form">
    <form method="post" action="${cp}/manage/warehouse/receipt-create" id="receipt-form">
        <div class="form-grid two">
            <label>Kho nhập <span style="color:red">*</span>
                <select name="warehouseId" required>
                    <option value="">-- Chọn kho --</option>
                    <c:forEach items="${warehouses}" var="w">
                        <option value="${w.warehouseId}">${w.warehouseName}</option>
                    </c:forEach>
                </select>
            </label>
            <label>Nhà cung cấp <span style="color:red">*</span>
                <input type="text" name="supplierName" required placeholder="Tên nhà cung cấp">
            </label>
            <label>SĐT Nhà cung cấp
                <input type="text" name="supplierPhone" placeholder="Số điện thoại">
            </label>
            <label class="full-field">Ghi chú
                <textarea name="note" placeholder="Nội dung ghi chú"></textarea>
            </label>
        </div>

        <div class="line-items" style="margin-top: 24px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                <b>Danh sách sản phẩm nhập <span style="color:red">*</span></b>
                <button type="button" class="button button-outline" onclick="addReceiptRow()">+ Thêm dòng</button>
            </div>
            <table id="receipt-table" style="width:100%">
                <thead>
                    <tr>
                        <th>Sản phẩm / Biến thể</th>
                        <th style="width:120px">Số lượng</th>
                        <th style="width:150px">Đơn giá nhập (₫)</th>
                        <th style="width:50px"></th>
                    </tr>
                </thead>
                <tbody id="receipt-items">
                    <tr class="item-row">
                        <td>
                            <select name="variantIds" required class="variant-select">
                                <option value="">-- Chọn biến thể --</option>
                                <c:forEach items="${variants}" var="v">
                                    <option value="${v.variantId}">${v.productName} – ${v.variantName} (${v.sku})</option>
                                </c:forEach>
                            </select>
                        </td>
                        <td><input type="number" name="quantities" min="1" required value="1" style="width:100%"></td>
                        <td><input type="number" name="unitCosts" min="0" step="1000" required value="0" style="width:100%"></td>
                        <td><button type="button" class="btn-remove" onclick="removeRow(this)" title="Xóa dòng">✕</button></td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="form-actions" style="margin-top: 20px;">
            <button type="submit" class="button button-gold">Tạo phiếu nháp (DRAFT)</button>
        </div>
    </form>
</div>

<style>
.btn-remove { background: #e74c3c; color: #fff; border: none; border-radius: 4px; padding: 4px 10px; cursor: pointer; }
.variant-select { width: 100%; }
</style>

<script>
// Danh sách options để clone
var variantOptions = '<option value="">-- Chọn biến thể --</option>' +
    document.querySelector('.variant-select').innerHTML.replace('<option value="">-- Chọn biến thể --</option>', '');

function addReceiptRow() {
    var tbody = document.getElementById('receipt-items');
    var row = document.createElement('tr');
    row.className = 'item-row';
    row.innerHTML =
        '<td><select name="variantIds" required class="variant-select">' + variantOptions + '</select></td>' +
        '<td><input type="number" name="quantities" min="1" required value="1" style="width:100%"></td>' +
        '<td><input type="number" name="unitCosts" min="0" step="1000" required value="0" style="width:100%"></td>' +
        '<td><button type="button" class="btn-remove" onclick="removeRow(this)" title="Xóa dòng">✕</button></td>';
    tbody.appendChild(row);
}

function removeRow(btn) {
    var rows = document.querySelectorAll('#receipt-items .item-row');
    if (rows.length === 1) { alert('Phiếu phải có ít nhất một sản phẩm.'); return; }
    btn.closest('tr').remove();
}
</script>
