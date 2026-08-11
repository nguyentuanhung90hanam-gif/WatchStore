<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="inventory-page">

    <!-- ==================== HEADER ==================== -->
    <div class="module-heading inventory-header">

        <div class="module-title-area">

            <p class="eyebrow dark">
                TỒN KHO
            </p>

            <h2>
                Quản lý Tồn kho
            </h2>

            <p class="module-desc">
                Theo dõi số lượng hàng hóa, tồn khả dụng và ngưỡng cảnh báo tồn kho.
            </p>

        </div>

        <div class="inventory-header-actions">

            <a href="${cp}/manage/warehouse/alerts"
               class="button button-outline">
                Cảnh báo tồn kho
            </a>

            <a href="${cp}/manage/warehouse/transactions"
               class="button button-outline">
                Lịch sử biến động
            </a>

        </div>

    </div>


    <!-- ==================== MESSAGE ==================== -->

    <c:if test="${not empty sessionScope.successMsg}">

        <div class="inventory-alert inventory-alert-success">
            <span class="inventory-alert-icon">✓</span>

            <span>
                ${sessionScope.successMsg}
            </span>
        </div>

        <c:remove var="successMsg" scope="session"/>

    </c:if>


    <c:if test="${not empty sessionScope.errorMsg}">

        <div class="inventory-alert inventory-alert-error">
            <span class="inventory-alert-icon">!</span>

            <span>
                ${sessionScope.errorMsg}
            </span>
        </div>

        <c:remove var="errorMsg" scope="session"/>

    </c:if>


    <!-- ==================== SEARCH ==================== -->

    <div class="dashboard-card inventory-filter-card">

        <div class="inventory-card-header">

            <div>
                <h3>Tra cứu tồn kho</h3>

                <p>
                    Tìm kiếm theo SKU, sản phẩm, biến thể hoặc kho.
                </p>
            </div>

        </div>

        <form method="get"
              action="${cp}/manage/warehouse/inventory"
              class="inventory-filter-form">

            <div class="inventory-field inventory-field-keyword">

                <label for="inventory-keyword">
                    SKU / sản phẩm / biến thể
                </label>

                <input
                    id="inventory-keyword"
                    name="keyword"
                    value="${param.keyword}"
                    placeholder="Nhập từ khóa..."
                    autocomplete="off"
                >

            </div>


            <div class="inventory-field inventory-field-warehouse">

                <label for="inventory-warehouse">
                    Kho
                </label>

                <select
                    id="inventory-warehouse"
                    name="warehouseId">

                    <option value="">
                        Tất cả kho
                    </option>

                    <c:forEach items="${warehouses}" var="w">

                        <option
                            value="${w.warehouseId}"
                            ${param.warehouseId == w.warehouseId ? 'selected' : ''}>
                            ${w.warehouseName}
                        </option>

                    </c:forEach>

                </select>

            </div>


            <div class="inventory-filter-actions">

                <button
                    class="button button-outline"
                    type="submit">
                    Lọc
                </button>

                <a
                    class="button button-outline"
                    href="${cp}/manage/warehouse/inventory">
                    Đặt lại
                </a>

            </div>

        </form>

    </div>


    <!-- ==================== MANUAL ADJUSTMENT ==================== -->

    <div class="dashboard-card inventory-adjust-card">

        <div class="inventory-card-header inventory-adjust-header">

            <div>

                <h3>
                    Điều chỉnh tồn kho thủ công
                </h3>

                <p>
                    Tạo biến động tồn kho trực tiếp cho một kho và một biến thể.
                </p>

            </div>

        </div>


        <form
            method="post"
            action="${cp}/manage/warehouse/adjust"
            class="inventory-adjust-form">


            <div class="inventory-field">

                <label for="adjust-warehouse">
                    Kho
                </label>

                <select
                    id="adjust-warehouse"
                    name="warehouseId"
                    required>

                    <option value="">
                        -- Chọn kho --
                    </option>

                    <c:forEach items="${warehouses}" var="w">

                        <option value="${w.warehouseId}">
                            ${w.warehouseName}
                        </option>

                    </c:forEach>

                </select>

            </div>


            <div class="inventory-field">

                <label for="adjust-variant">
                    Biến thể
                </label>

                <select
                    id="adjust-variant"
                    name="variantId"
                    required>

                    <option value="">
                        -- Chọn biến thể --
                    </option>

                    <c:forEach items="${variants}" var="v">

                        <option value="${v.variantId}">
                            ${v.sku} - ${v.productName}
                        </option>

                    </c:forEach>

                </select>

            </div>


            <div class="inventory-field">

                <label for="adjust-quantity">
                    Số lượng thay đổi
                </label>

                <input
                    id="adjust-quantity"
                    type="number"
                    name="quantityChange"
                    required
                    placeholder="+10 hoặc -5"
                    step="1"
                >

            </div>


            <div class="inventory-field inventory-field-note">

                <label for="adjust-note">
                    Lý do điều chỉnh
                </label>

                <input
                    id="adjust-note"
                    name="note"
                    maxlength="500"
                    placeholder="Nhập lý do điều chỉnh..."
                >

            </div>


            <div class="inventory-adjust-submit">

                <button
                    class="button button-gold"
                    type="submit"
                    onclick="return confirm('Xác nhận điều chỉnh tồn kho?')">
                    Điều chỉnh
                </button>

            </div>

        </form>


        <div class="inventory-adjust-note">

            <span class="inventory-note-label">
                Lưu ý:
            </span>

            <span>
                Số dương = ADJUST_IN, số âm = ADJUST_OUT.
                Hệ thống không cho phép tồn kho sau điều chỉnh âm.
            </span>

        </div>

    </div>


    <!-- ==================== INVENTORY TABLE ==================== -->

    <div class="dashboard-card inventory-table-card">

        <div class="inventory-card-header inventory-table-header">

            <div>

                <h3>
                    Danh sách tồn kho
                </h3>

                <p>
                    Theo dõi tồn thực tế, tồn khả dụng và trạng thái từng mặt hàng.
                </p>

            </div>

            <div class="inventory-count">

                <c:choose>

                    <c:when test="${not empty inventoryItems}">
                        ${inventoryItems.size()} mặt hàng
                    </c:when>

                    <c:otherwise>
                        0 mặt hàng
                    </c:otherwise>

                </c:choose>

            </div>

        </div>


        <div class="table-wrap inventory-table-wrap">

            <table class="inventory-table">

                <thead>

                    <tr>

                        <th class="col-warehouse">
                            Kho
                        </th>

                        <th class="col-sku">
                            SKU
                        </th>

                        <th class="col-product">
                            Sản phẩm / Biến thể
                        </th>

                        <th class="col-number">
                            Tồn thực tế
                        </th>

                        <th class="col-number">
                            Đã đặt trước
                        </th>

                        <th class="col-number">
                            Tồn khả dụng
                        </th>

                        <th class="col-number">
                            Mức cảnh báo
                        </th>

                        <th class="col-status">
                            Trạng thái
                        </th>

                    </tr>

                </thead>


                <tbody>

                    <c:forEach items="${inventoryItems}" var="i">

                        <tr>

                            <td class="inventory-warehouse-cell">

                                <span class="warehouse-name">
                                    ${i.warehouseName}
                                </span>

                            </td>


                            <td>

                                <span class="inventory-sku">
                                    ${i.sku}
                                </span>

                            </td>


                            <td class="inventory-product-cell">

                                <span class="inventory-product-name">
                                    ${i.productName}
                                </span>

                                <span class="inventory-variant-name">
                                    ${i.variantName}
                                </span>

                            </td>


                            <td class="inventory-number">

                                ${i.quantityOnHand}

                            </td>


                            <td class="inventory-number">

                                ${i.quantityReserved}

                            </td>


                            <td class="inventory-number">

                                <span
                                    class="${i.availableQuantity <= 0
                                        ? 'quantity-danger'
                                        : i.availableQuantity <= i.reorderLevel
                                            ? 'quantity-warning'
                                            : 'quantity-safe'}">

                                    ${i.availableQuantity}

                                </span>

                            </td>


                            <td class="inventory-number">

                                <span class="reorder-level">
                                    ${i.reorderLevel}
                                </span>

                            </td>


                            <td class="inventory-status-cell">

                                <c:choose>

                                    <c:when test="${i.availableQuantity <= 0}">

                                        <span class="status-badge danger">
                                            HẾT HÀNG
                                        </span>

                                    </c:when>


                                    <c:when test="${i.availableQuantity <= i.reorderLevel}">

                                        <span class="status-badge warning">
                                            SẮP HẾT
                                        </span>

                                    </c:when>


                                    <c:otherwise>

                                        <span class="status-badge success">
                                            AN TOÀN
                                        </span>

                                    </c:otherwise>

                                </c:choose>

                            </td>

                        </tr>

                    </c:forEach>


                    <c:if test="${empty inventoryItems}">

                        <tr>

                            <td
                                colspan="8"
                                class="inventory-empty">

                                <div class="inventory-empty-icon">
                                    —
                                </div>

                                <strong>
                                    Chưa có dữ liệu tồn kho
                                </strong>

                                <span>
                                    Không tìm thấy sản phẩm phù hợp với điều kiện tra cứu.
                                </span>

                            </td>

                        </tr>

                    </c:if>

                </tbody>

            </table>

        </div>

    </div>

</div>


<style>

/* =========================================================
   INVENTORY PAGE
   ========================================================= */

.inventory-page {
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
}


/* =========================================================
   HEADER
   ========================================================= */

.inventory-header {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 24px;
    margin-bottom: 22px;
}

.inventory-header .module-title-area {
    min-width: 0;
}

.inventory-header-actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
    flex-wrap: wrap;
    flex-shrink: 0;
}


/* =========================================================
   MESSAGE
   ========================================================= */

.inventory-alert {
    display: flex;
    align-items: center;
    gap: 10px;
    min-height: 42px;
    padding: 10px 14px;
    margin-bottom: 16px;
    border: 1px solid transparent;
    border-radius: 8px;
    box-sizing: border-box;
    font-size: 13px;
}

.inventory-alert-success {
    background: #f0fdf4;
    border-color: #bbf7d0;
    color: #166534;
}

.inventory-alert-error {
    background: #fef2f2;
    border-color: #fecaca;
    color: #991b1b;
}

.inventory-alert-icon {
    width: 20px;
    height: 20px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    font-size: 12px;
    font-weight: 700;
    flex-shrink: 0;
}

.inventory-alert-success .inventory-alert-icon {
    background: #dcfce7;
}

.inventory-alert-error .inventory-alert-icon {
    background: #fee2e2;
}


/* =========================================================
   CARD
   ========================================================= */

.inventory-filter-card,
.inventory-adjust-card,
.inventory-table-card {
    width: 100%;
    box-sizing: border-box;
    margin-bottom: 16px;
}

.inventory-table-card {
    margin-bottom: 0;
}

.inventory-card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
    margin-bottom: 16px;
}

.inventory-card-header h3 {
    margin: 0;
    font-size: 16px;
    line-height: 1.4;
    font-weight: 700;
}

.inventory-card-header p {
    margin: 4px 0 0;
    color: #777;
    font-size: 12px;
    line-height: 1.5;
}


/* =========================================================
   FILTER
   ========================================================= */

.inventory-filter-form {
    display: grid;
    grid-template-columns: minmax(280px, 1fr) minmax(220px, 280px) auto;
    align-items: end;
    gap: 12px;
}

.inventory-field {
    min-width: 0;
}

.inventory-field label {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin: 0;
    font-size: 12px;
    font-weight: 600;
    color: #444;
}

.inventory-field input,
.inventory-field select {
    width: 100%;
    height: 40px;
    box-sizing: border-box;
    padding: 0 11px;
    border: 1px solid #d7d7d7;
    border-radius: 6px;
    background: #fff;
    color: #333;
    font-family: inherit;
    font-size: 13px;
    outline: none;
    transition:
        border-color .15s ease,
        box-shadow .15s ease;
}

.inventory-field input:focus,
.inventory-field select:focus {
    border-color: #b89a5d;
    box-shadow: 0 0 0 3px rgba(184, 154, 93, .10);
}

.inventory-filter-actions {
    display: flex;
    align-items: center;
    gap: 8px;
    height: 40px;
}

.inventory-filter-actions .button {
    white-space: nowrap;
}


/* =========================================================
   MANUAL ADJUSTMENT
   ========================================================= */

.inventory-adjust-header {
    margin-bottom: 18px;
}

.inventory-adjust-form {
    display: grid;
    grid-template-columns:
        minmax(170px, 1fr)
        minmax(220px, 1.3fr)
        minmax(150px, .8fr)
        minmax(220px, 1.4fr)
        auto;
    align-items: end;
    gap: 12px;
}

.inventory-adjust-submit {
    display: flex;
    align-items: flex-end;
    height: 40px;
}

.inventory-adjust-submit .button {
    height: 40px;
    white-space: nowrap;
}

.inventory-adjust-note {
    display: flex;
    align-items: flex-start;
    gap: 6px;
    margin-top: 12px;
    padding: 9px 11px;
    border-radius: 6px;
    background: #faf8f3;
    color: #777;
    font-size: 11px;
    line-height: 1.5;
}

.inventory-note-label {
    color: #8a6d35;
    font-weight: 700;
    flex-shrink: 0;
}


/* =========================================================
   INVENTORY TABLE HEADER
   ========================================================= */

.inventory-table-header {
    margin-bottom: 0;
    padding-bottom: 14px;
    border-bottom: 1px solid #ededed;
}

.inventory-count {
    flex-shrink: 0;
    padding: 5px 9px;
    border: 1px solid #e5e5e5;
    border-radius: 5px;
    background: #fafafa;
    color: #666;
    font-size: 11px;
    font-weight: 600;
}


/* =========================================================
   TABLE
   ========================================================= */

.inventory-table-wrap {
    width: 100%;
    overflow-x: auto;
    overflow-y: hidden;
}

.inventory-table {
    width: 100%;
    min-width: 900px;
    border-collapse: collapse;
    table-layout: fixed;
}

.inventory-table thead th {
    height: 42px;
    padding: 0 12px;
    background: #fafafa;
    border-bottom: 1px solid #dedede;
    color: #666;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: .02em;
    text-transform: uppercase;
    vertical-align: middle;
    white-space: nowrap;
}

.inventory-table tbody td {
    padding: 13px 12px;
    border-bottom: 1px solid #eeeeee;
    color: #333;
    font-size: 13px;
    vertical-align: middle;
}

.inventory-table tbody tr {
    transition: background-color .12s ease;
}

.inventory-table tbody tr:hover {
    background: #fcfbf8;
}

.inventory-table tbody tr:last-child td {
    border-bottom: none;
}


/* =========================================================
   COLUMN WIDTH
   ========================================================= */

.inventory-table .col-warehouse {
    width: 15%;
}

.inventory-table .col-sku {
    width: 12%;
}

.inventory-table .col-product {
    width: 27%;
}

.inventory-table .col-number {
    width: 8.5%;
    text-align: right;
}

.inventory-table .col-status {
    width: 12%;
    text-align: center;
}


/* =========================================================
   TABLE CONTENT
   ========================================================= */

.inventory-warehouse-cell {
    color: #444;
}

.warehouse-name {
    display: block;
    line-height: 1.4;
    font-weight: 600;
}

.inventory-sku {
    display: inline-block;
    padding: 4px 6px;
    border-radius: 4px;
    background: #f5f5f5;
    color: #333;
    font-family: monospace;
    font-size: 11px;
    font-weight: 700;
    white-space: nowrap;
}

.inventory-product-cell {
    min-width: 0;
}

.inventory-product-name {
    display: block;
    overflow: hidden;
    color: #333;
    font-weight: 600;
    line-height: 1.4;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.inventory-variant-name {
    display: block;
    margin-top: 3px;
    overflow: hidden;
    color: #888;
    font-size: 11px;
    line-height: 1.4;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.inventory-number {
    text-align: right;
    font-variant-numeric: tabular-nums;
    font-weight: 600;
}

.inventory-status-cell {
    text-align: center;
}


/* =========================================================
   QUANTITY
   ========================================================= */

.quantity-safe {
    color: #16803c;
    font-weight: 700;
}

.quantity-warning {
    color: #a16207;
    font-weight: 700;
}

.quantity-danger {
    color: #c62828;
    font-weight: 700;
}

.reorder-level {
    color: #555;
    font-weight: 600;
}


/* =========================================================
   STATUS
   ========================================================= */

.status-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 72px;
    height: 24px;
    padding: 0 8px;
    box-sizing: border-box;
    border-radius: 5px;
    font-size: 10px;
    font-weight: 700;
    letter-spacing: .02em;
    white-space: nowrap;
}

.status-badge.success {
    background: #ecfdf3;
    border: 1px solid #bbf7d0;
    color: #15803d;
}

.status-badge.warning {
    background: #fffbeb;
    border: 1px solid #fde68a;
    color: #a16207;
}

.status-badge.danger {
    background: #fef2f2;
    border: 1px solid #fecaca;
    color: #b91c1c;
}


/* =========================================================
   EMPTY STATE
   ========================================================= */

.inventory-empty {
    padding: 44px 20px !important;
    text-align: center;
    color: #777 !important;
}

.inventory-empty-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 38px;
    height: 38px;
    margin: 0 auto 10px;
    border: 1px solid #ddd;
    border-radius: 50%;
    color: #999;
    font-size: 18px;
}

.inventory-empty strong {
    display: block;
    margin-bottom: 4px;
    color: #555;
    font-size: 13px;
}

.inventory-empty span {
    display: block;
    color: #999;
    font-size: 11px;
}


/* =========================================================
   RESPONSIVE
   ========================================================= */

@media (max-width: 1200px) {

    .inventory-adjust-form {
        grid-template-columns:
            1fr
            1fr
            1fr;
    }

    .inventory-field-note {
        grid-column: span 2;
    }

    .inventory-adjust-submit {
        grid-column: span 1;
    }

}


@media (max-width: 900px) {

    .inventory-header {
        align-items: flex-start;
        flex-direction: column;
    }

    .inventory-header-actions {
        justify-content: flex-start;
        width: 100%;
    }

    .inventory-filter-form {
        grid-template-columns: 1fr 1fr;
    }

    .inventory-field-keyword {
        grid-column: span 2;
    }

    .inventory-filter-actions {
        grid-column: span 2;
    }

}


@media (max-width: 700px) {

    .inventory-header-actions {
        display: grid;
        grid-template-columns: 1fr 1fr;
        width: 100%;
    }

    .inventory-header-actions .button {
        width: 100%;
        text-align: center;
        box-sizing: border-box;
    }

    .inventory-filter-form {
        grid-template-columns: 1fr;
    }

    .inventory-field-keyword,
    .inventory-filter-actions {
        grid-column: auto;
    }

    .inventory-filter-actions {
        width: 100%;
    }

    .inventory-filter-actions .button {
        flex: 1;
        text-align: center;
    }

    .inventory-adjust-form {
        grid-template-columns: 1fr;
    }

    .inventory-field-note,
    .inventory-adjust-submit {
        grid-column: auto;
    }

    .inventory-adjust-submit .button {
        width: 100%;
    }

    .inventory-adjust-note {
        flex-direction: column;
        gap: 2px;
    }

    .inventory-card-header {
        align-items: flex-start;
        flex-direction: column;
    }

    .inventory-count {
        align-self: flex-start;
    }

}


@media (max-width: 480px) {

    .inventory-header-actions {
        grid-template-columns: 1fr;
    }

    .inventory-table {
        min-width: 900px;
    }

}

</style>