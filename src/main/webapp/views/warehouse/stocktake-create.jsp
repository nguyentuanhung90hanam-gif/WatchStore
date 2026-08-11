<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">KIỂM KÊ KHO</p>
        <h2>Tạo Phiếu Kiểm Kê</h2>
        <p class="module-desc">Chọn kho, thêm sản phẩm và nhập số lượng thực tế ngay khi tạo phiếu.</p>
    </div>
</div>

<c:if test="${not empty sessionScope.errorMsg}">
    <div class="alert alert-error">${sessionScope.errorMsg}</div>
    <c:remove var="errorMsg" scope="session"/>
</c:if>

<div class="dashboard-card" style="max-width: 1000px; margin: 0 auto;">
    <form action="${cp}/manage/warehouse/stocktake-create" method="POST" id="stocktakeCreateForm">
        <div class="form-grid" style="display:grid; grid-template-columns: 1fr; gap:20px; margin-bottom:24px;">
            <div class="form-group">
                <label>Kho hàng *</label>
                <select name="warehouseId" class="input-field" required>
                    <option value="">-- Chọn kho --</option>
                    <c:forEach items="${warehouses}" var="w">
                        <option value="${w.warehouseId}">${w.warehouseName}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label>Ghi chú / Lý do kiểm kê</label>
                <textarea name="note" class="input-field" rows="3" placeholder="Ví dụ: Kiểm kê định kỳ cuối tháng"></textarea>
            </div>
        </div>

        <div style="padding:16px; margin-bottom:24px; background:#f8f9fa; border:1px solid #eee; border-radius:6px;">
            <strong>Sản phẩm kiểm kê *</strong>
            <p style="margin:6px 0 0; color:#666;">Phải có ít nhất một sản phẩm. Bạn có thể thêm nhiều biến thể, nhưng không được chọn trùng biến thể.</p>
        </div>

        <div id="itemRows">
            <div class="stocktake-item-row" style="display:grid; grid-template-columns:minmax(0, 1fr) 180px 90px; gap:10px; align-items:end; margin-bottom:12px;">
                <div class="form-group" style="margin:0;">
                    <label>Sản phẩm *</label>
                    <select name="variantId" class="input-field variant-select" required>
                        <option value="">-- Chọn sản phẩm --</option>
                        <c:forEach items="${variants}" var="v">
                            <option value="${v.variantId}">${v.sku} - ${v.productName} (${v.variantName})</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group" style="margin:0;">
                    <label>SL thực tế *</label>
                    <input type="number" name="actualQuantity" class="input-field" min="0" step="1" required value="0">
                </div>

                <button type="button" class="button button-outline remove-row" style="display:none;">Xóa</button>
            </div>
        </div>

        <div style="margin-bottom:24px;">
            <button type="button" class="button button-outline" id="addItemRow">+ Thêm sản phẩm</button>
        </div>

        <div style="padding:16px; margin-bottom:24px; background:#fff8e1; border:1px solid #f0df9b; border-radius:6px;">
            <strong>Quy trình:</strong> Tạo phiếu ở trạng thái <b>DRAFT</b>. Sau khi tạo, bạn vẫn có thể thêm, sửa hoặc xóa sản phẩm tại trang chi tiết trước khi bấm <b>Gửi duyệt (Xác nhận Đếm)</b>.
        </div>

        <div style="display:flex; justify-content:flex-end; gap:12px; border-top:1px solid #eee; padding-top:20px;">
            <a href="${cp}/manage/warehouse/stocktake" class="button button-outline">Hủy bỏ</a>
            <button type="submit" class="button button-gold">Tạo Phiếu Kiểm Kê</button>
        </div>
    </form>
</div>

<style>
.alert { padding:12px 16px; border-radius:6px; margin-bottom:16px; }
.alert-error { background:#f8d7da; color:#721c24; border:1px solid #f5c6cb; }
</style>

<script>
(function () {
    const rows = document.getElementById('itemRows');
    const addButton = document.getElementById('addItemRow');

    if (!rows || !addButton) {
        return;
    }

    function updateRemoveButtons() {
        const rowList = rows.querySelectorAll('.stocktake-item-row');
        rowList.forEach(function (row) {
            const button = row.querySelector('.remove-row');
            if (button) {
                button.style.display = rowList.length > 1 ? 'inline-block' : 'none';
            }
        });
    }

    addButton.addEventListener('click', function () {
        const firstRow = rows.querySelector('.stocktake-item-row');
        const newRow = firstRow.cloneNode(true);

        const select = newRow.querySelector('.variant-select');
        const quantity = newRow.querySelector('input[name="actualQuantity"]');
        const removeButton = newRow.querySelector('.remove-row');

        if (select) {
            select.selectedIndex = 0;
        }

        if (quantity) {
            quantity.value = '0';
        }

        if (removeButton) {
            removeButton.style.display = 'inline-block';
            removeButton.addEventListener('click', function () {
                newRow.remove();
                updateRemoveButtons();
            });
        }

        rows.appendChild(newRow);
        updateRemoveButtons();
    });

    rows.querySelector('.remove-row').addEventListener('click', function () {
        this.closest('.stocktake-item-row').remove();
        updateRemoveButtons();
    });
})();
</script>
