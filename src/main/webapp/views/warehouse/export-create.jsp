<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">XUẤT KHO</p>
        <h2>Tạo phiếu xuất mới</h2>
        <p class="module-desc">Chỉ chọn những biến thể đang có tồn khả dụng tại kho xuất. Phiếu được lưu DRAFT để kiểm tra lại trước khi gửi duyệt.</p>
    </div>
    <a class="button button-outline" href="${cp}/manage/warehouse/exports">Quay lại</a>
</div>

<c:if test="${not empty sessionScope.errorMsg}">
    <div class="alert alert-error">${sessionScope.errorMsg}</div>
    <c:remove var="errorMsg" scope="session"/>
</c:if>

<div class="dashboard-card module-form">
    <form method="post" action="${cp}/manage/warehouse/export-create" id="export-form">
        <div class="form-grid two">
            <label>Loại xuất <span style="color:red">*</span>
                <select name="exportType" required>
                    <option value="SALE">Xuất Bán (SALE)</option>
                    <option value="TRANSFER">Chuyển Kho (TRANSFER)</option>
                    <option value="DAMAGED">Xuất Hủy (DAMAGED)</option>
                    <option value="OTHER">Khác (OTHER)</option>
                </select>
            </label>
            <label>Kho xuất <span style="color:red">*</span>
                <select name="warehouseId" id="warehouseId" required>
                    <option value="">-- Chọn kho --</option>
                    <c:forEach items="${warehouses}" var="w">
                        <option value="${w.warehouseId}">${w.warehouseName}</option>
                    </c:forEach>
                </select>
            </label>
            <label>Người nhận <span style="color:red">*</span>
                <input type="text" name="receiverName" required placeholder="Tên người nhận / chi nhánh">
            </label>
            <label id="order-field">OrderID (chỉ dùng cho Xuất Bán)
                <input type="number" name="orderId" id="orderId" min="1" placeholder="Nhập OrderID tồn tại trong Orders" disabled>
                <small id="order-help" style="display:block; margin-top:6px; color:#777;">Chỉ khi chọn SALE mới nhập OrderID. SKU và VariantID không phải OrderID.</small>
            </label>
            <label class="full-field">Ghi chú
                <textarea name="note" placeholder="Nội dung ghi chú"></textarea>
            </label>
        </div>

        <div class="line-items" style="margin-top:24px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
                <div>
                    <b>Danh sách sản phẩm xuất <span style="color:red">*</span></b>
                    <small style="display:block;color:#777;margin-top:4px;">Danh sách lấy từ InventoryBalances theo kho đã chọn.</small>
                </div>
                <button type="button" class="button button-outline" onclick="addExportRow()">+ Thêm dòng</button>
            </div>
            <table style="width:100%">
                <thead>
                    <tr>
                        <th>Sản phẩm / Biến thể</th>
                        <th style="width:120px">Tồn khả dụng</th>
                        <th style="width:130px">Số lượng xuất</th>
                        <th style="width:50px"></th>
                    </tr>
                </thead>
                <tbody id="export-items">
                    <tr class="item-row">
                        <td>
                            <select name="variantIds" required class="variant-select" onchange="syncAvailable(this)">
                                <option value="">-- Chọn kho trước --</option>
                                <c:forEach items="${inventoryItems}" var="i">
                                    <c:if test="${i.availableQuantity > 0}">
                                        <option value="${i.variantId}" data-warehouse="${i.warehouseId}" data-available="${i.availableQuantity}">${i.productName} – ${i.variantName} (${i.sku}) | Tồn: ${i.availableQuantity}</option>
                                    </c:if>
                                </c:forEach>
                            </select>
                        </td>
                        <td class="available-cell" style="text-align:right;">—</td>
                        <td><input type="number" name="quantities" min="1" required value="1" style="width:100%" oninput="validateQuantity(this)"></td>
                        <td><button type="button" class="btn-remove" onclick="removeRow(this)" title="Xóa">✕</button></td>
                    </tr>
                </tbody>
            </table>
            <div id="stock-message" class="stock-message" style="margin-top:10px;"></div>
        </div>

        <div class="form-actions" style="margin-top:20px;">
            <button type="submit" class="button button-gold">Tạo phiếu nháp (DRAFT)</button>
        </div>
    </form>
</div>

<style>
.btn-remove { background:#e74c3c; color:#fff; border:none; border-radius:4px; padding:4px 10px; cursor:pointer; }
.variant-select { width:100%; }
.alert { padding:12px 16px; border-radius:6px; margin-bottom:16px; }
.alert-error { background:#f8d7da; color:#721c24; border:1px solid #f5c6cb; }
.stock-message { color:#777; font-size:13px; }
</style>

<script>

function syncOrderField() {
    var type = document.querySelector('select[name="exportType"]').value;
    var input = document.getElementById('orderId');
    var help = document.getElementById('order-help');
    var sale = type === 'SALE';

    input.disabled = !sale;
    input.required = sale;

    if (!sale) {
        input.value = '';
        input.setCustomValidity('');
        help.textContent = 'Loại xuất này không liên kết với Orders. Không cần nhập OrderID.';
    } else {
        help.textContent = 'Nhập đúng OrderID tồn tại trong bảng Orders. Đây là ID đơn hàng, không phải SKU/VariantID.';
    }
}

document.querySelector('select[name="exportType"]').addEventListener('change', syncOrderField);

var allOptions = [];
document.querySelectorAll('.variant-select option[data-warehouse]').forEach(function(option) {
    allOptions.push({
        value: option.value,
        warehouse: option.dataset.warehouse,
        available: option.dataset.available,
        text: option.textContent
    });
});

function rebuildSelect(select) {
    var warehouseId = document.getElementById('warehouseId').value;
    var current = select.value;
    select.innerHTML = '<option value="">' + (warehouseId ? '-- Chọn biến thể --' : '-- Chọn kho trước --') + '</option>';
    allOptions.forEach(function(item) {
        if (item.warehouse === warehouseId) {
            var option = document.createElement('option');
            option.value = item.value;
            option.dataset.warehouse = item.warehouse;
            option.dataset.available = item.available;
            option.textContent = item.text;
            select.appendChild(option);
        }
    });
    if (current) {
        select.value = current;
    }
    syncAvailable(select);
}

function rebuildAllRows() {
    document.querySelectorAll('.variant-select').forEach(rebuildSelect);
    var count = document.querySelectorAll('#export-items .item-row').length;
    var warehouseId = document.getElementById('warehouseId').value;
    document.getElementById('stock-message').textContent = warehouseId && allOptions.every(function(x){ return x.warehouse !== warehouseId; })
        ? 'Kho này hiện chưa có biến thể nào có tồn khả dụng để xuất.'
        : '';
}

document.getElementById('warehouseId').addEventListener('change', function() {
    document.querySelectorAll('.variant-select').forEach(function(select) { select.value = ''; rebuildSelect(select); });
});

function syncAvailable(select) {
    var row = select.closest('tr');
    var option = select.options[select.selectedIndex];
    var availableCell = row.querySelector('.available-cell');
    var quantityInput = row.querySelector('input[name="quantities"]');
    var available = option && option.dataset.available ? parseInt(option.dataset.available, 10) : 0;
    availableCell.textContent = available > 0 ? available : '—';
    quantityInput.max = available > 0 ? available : '';
    validateQuantity(quantityInput);
}

function validateQuantity(input) {
    var max = parseInt(input.max || '0', 10);
    if (max > 0 && parseInt(input.value || '0', 10) > max) {
        input.setCustomValidity('Số lượng xuất không được vượt quá tồn khả dụng (' + max + ').');
    } else {
        input.setCustomValidity('');
    }
}

function addExportRow() {
    var tbody = document.getElementById('export-items');
    var first = tbody.querySelector('.item-row');
    var row = first.cloneNode(true);
    var select = row.querySelector('.variant-select');
    select.value = '';
    row.querySelector('.available-cell').textContent = '—';
    row.querySelector('input[name="quantities"]').value = '1';
    row.querySelector('input[name="quantities"]').max = '';
    tbody.appendChild(row);
    rebuildSelect(select);
}

function removeRow(btn) {
    var rows = document.querySelectorAll('#export-items .item-row');
    if (rows.length === 1) { alert('Phiếu phải có ít nhất một sản phẩm.'); return; }
    btn.closest('tr').remove();
}

rebuildAllRows();
syncOrderField();
</script>
