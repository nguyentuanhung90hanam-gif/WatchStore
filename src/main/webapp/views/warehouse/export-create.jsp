<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">XUẤT KHO</p>
        <h2>Tạo phiếu xuất mới</h2>
        <p class="module-desc">Phiếu sẽ được lưu ở trạng thái DRAFT để kiểm tra tồn kho trước khi gửi duyệt.</p>
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
                <select name="warehouseId" required>
                    <option value="">-- Chọn kho --</option>
                    <c:forEach items="${warehouses}" var="w">
                        <option value="${w.warehouseId}">${w.warehouseName}</option>
                    </c:forEach>
                </select>
            </label>
            <label>Người nhận <span style="color:red">*</span>
                <input type="text" name="receiverName" required placeholder="Tên người nhận / chi nhánh">
            </label>
            <label>Mã Đơn Hàng (nếu có)
                <input type="number" name="orderId" placeholder="ID đơn hàng">
            </label>
            <label class="full-field">Ghi chú
                <textarea name="note" placeholder="Nội dung ghi chú"></textarea>
            </label>
        </div>

        <div class="line-items" style="margin-top:24px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
                <b>Danh sách sản phẩm xuất <span style="color:red">*</span></b>
                <button type="button" class="button button-outline" onclick="addExportRow()">+ Thêm dòng</button>
            </div>
            <table style="width:100%">
                <thead>
                    <tr>
                        <th>Sản phẩm / Biến thể</th>
                        <th style="width:130px">Số lượng xuất</th>
                        <th style="width:50px"></th>
                    </tr>
                </thead>
                <tbody id="export-items">
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
                        <td><button type="button" class="btn-remove" onclick="removeRow(this)" title="Xóa">✕</button></td>
                    </tr>
                </tbody>
            </table>
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
</style>

<script>
var variantOptions = document.querySelector('.variant-select').innerHTML
    .replace('<option value="">-- Chọn biến thể --</option>', '');

function addExportRow() {
    var tbody = document.getElementById('export-items');
    var row = document.createElement('tr');
    row.className = 'item-row';
    row.innerHTML =
        '<td><select name="variantIds" required class="variant-select">' +
            '<option value="">-- Chọn biến thể --</option>' + variantOptions +
        '</select></td>' +
        '<td><input type="number" name="quantities" min="1" required value="1" style="width:100%"></td>' +
        '<td><button type="button" class="btn-remove" onclick="removeRow(this)">✕</button></td>';
    tbody.appendChild(row);
}

function removeRow(btn) {
    var rows = document.querySelectorAll('#export-items .item-row');
    if (rows.length === 1) { alert('Phiếu phải có ít nhất một sản phẩm.'); return; }
    btn.closest('tr').remove();
}
</script>
