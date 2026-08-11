<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="module-heading">

    <div class="module-title-area">

        <p class="eyebrow dark">BÁO CÁO</p>

        <h2>Báo cáo kho</h2>

        <p class="module-desc">
            Báo cáo tồn kho, nhập, xuất và hàng hư hỏng theo khoảng thời gian và kho.
        </p>

    </div>

</div>


<!-- ==================== BỘ LỌC BÁO CÁO ==================== -->

<form method="get"
      action="${cp}/manage/warehouse/reports"
      class="dashboard-card warehouse-report-filter">

    <div class="report-filter-grid">

        <label>
            Loại báo cáo

            <select name="type">

                <option value="inventory"
                    ${selectedType == 'inventory' ? 'selected' : ''}>
                    Tồn kho
                </option>

                <option value="receipts"
                    ${selectedType == 'receipts' ? 'selected' : ''}>
                    Nhập kho
                </option>

                <option value="exports"
                    ${selectedType == 'exports' ? 'selected' : ''}>
                    Xuất kho
                </option>

                <option value="damaged"
                    ${selectedType == 'damaged' ? 'selected' : ''}>
                    Hàng hư hỏng
                </option>

            </select>

        </label>


        <label>
            Từ ngày
            <input type="date"
                   name="from"
                   value="${from}">
        </label>


        <label>
            Đến ngày
            <input type="date"
                   name="to"
                   value="${to}">
        </label>


        <label>
            Kho

            <select name="warehouseId">

                <option value="">Tất cả kho</option>

                <c:forEach items="${warehouses}" var="w">

                    <option value="${w.warehouseId}"
                        ${selectedWarehouseId == w.warehouseId ? 'selected' : ''}>
                        ${w.warehouseName}
                    </option>

                </c:forEach>

            </select>

        </label>


        <div class="report-filter-action">

            <button class="button button-gold"
                    type="submit">
                Xem báo cáo
            </button>

        </div>

    </div>

</form>


<!-- ==================== BÁO CÁO TỒN KHO ==================== -->

<c:if test="${selectedType == 'inventory'}">

    <div class="dashboard-card report-card">

        <div class="report-card-header">

            <div>
                <p class="report-eyebrow">TỒN KHO</p>
                <h3>Báo cáo tồn kho</h3>
            </div>

            <span class="report-count">
                ${empty reportInventory ? 0 : reportInventory.size()}
                dòng
            </span>

        </div>


        <div class="table-wrap">

            <table>

                <thead>

                    <tr>

                        <th>Kho</th>

                        <th>SKU</th>

                        <th>Sản phẩm</th>

                        <th>Tồn</th>

                        <th>Đặt trước</th>

                        <th>Khả dụng</th>

                        <th>Ngưỡng</th>

                        <th>Trạng thái</th>

                    </tr>

                </thead>


                <tbody>

                    <c:forEach items="${reportInventory}" var="i">

                        <tr>

                            <td>
                                ${i.warehouseName}
                            </td>

                            <td>
                                <strong>${i.sku}</strong>
                            </td>

                            <td>

                                <strong>
                                    ${i.productName}
                                </strong>

                                <br>

                                <small>
                                    ${i.variantName}
                                </small>

                            </td>

                            <td class="report-number">
                                ${i.quantityOnHand}
                            </td>

                            <td class="report-number">
                                ${i.quantityReserved}
                            </td>

                            <td class="report-number report-highlight">
                                ${i.availableQuantity}
                            </td>

                            <td class="report-number">
                                ${i.reorderLevel}
                            </td>

                            <td>
                                ${i.stockStatus}
                            </td>

                        </tr>

                    </c:forEach>


                    <c:if test="${empty reportInventory}">

                        <tr>

                            <td colspan="8"
                                class="report-empty">

                                Không có dữ liệu tồn kho.

                            </td>

                        </tr>

                    </c:if>

                </tbody>

            </table>

        </div>

    </div>

</c:if>


<!-- ==================== BÁO CÁO NHẬP KHO ==================== -->

<c:if test="${selectedType == 'receipts'}">

    <div class="dashboard-card report-card">

        <div class="report-card-header">

            <div>

                <p class="report-eyebrow">NHẬP KHO</p>

                <h3>
                    Báo cáo nhập kho
                </h3>

                <span class="report-subtitle">
                    Chỉ hiển thị các phiếu đã hoàn tất.
                </span>

            </div>

            <span class="report-count">
                ${empty reportReceipts ? 0 : reportReceipts.size()}
                phiếu
            </span>

        </div>


        <div class="table-wrap">

            <table>

                <thead>

                    <tr>

                        <th>Mã phiếu</th>

                        <th>Ngày</th>

                        <th>Kho</th>

                        <th>Nhà cung cấp</th>

                        <th class="text-right">
                            Tổng tiền
                        </th>

                    </tr>

                </thead>


                <tbody>

                    <c:forEach items="${reportReceipts}" var="r">

                        <tr>

                            <td>
                                <strong>
                                    ${r.receiptCode}
                                </strong>
                            </td>

                            <td>
                                ${r.receiptDate}
                            </td>

                            <td>
                                ${r.warehouseName}
                            </td>

                            <td>
                                ${r.supplierName}
                            </td>

                            <td class="text-right report-money">

                                <fmt:formatNumber
                                    value="${r.totalCost}"
                                    pattern="#,##0"/>

                                ₫

                            </td>

                        </tr>

                    </c:forEach>


                    <c:if test="${empty reportReceipts}">

                        <tr>

                            <td colspan="5"
                                class="report-empty">

                                Không có dữ liệu nhập kho.

                            </td>

                        </tr>

                    </c:if>

                </tbody>

            </table>

        </div>

    </div>

</c:if>


<!-- ==================== BÁO CÁO XUẤT KHO ==================== -->

<c:if test="${selectedType == 'exports'}">

    <div class="dashboard-card report-card">

        <div class="report-card-header">

            <div>

                <p class="report-eyebrow">XUẤT KHO</p>

                <h3>
                    Báo cáo xuất kho
                </h3>

                <span class="report-subtitle">
                    Chỉ hiển thị các phiếu đã hoàn tất.
                </span>

            </div>

            <span class="report-count">
                ${empty reportExports ? 0 : reportExports.size()}
                phiếu
            </span>

        </div>


        <div class="table-wrap">

            <table>

                <thead>

                    <tr>

                        <th>Mã phiếu</th>

                        <th>Ngày</th>

                        <th>Kho</th>

                        <th>Loại</th>

                        <th>Người nhận</th>

                    </tr>

                </thead>


                <tbody>

                    <c:forEach items="${reportExports}" var="e">

                        <tr>

                            <td>
                                <strong>
                                    ${e.exportCode}
                                </strong>
                            </td>

                            <td>
                                ${e.exportDate}
                            </td>

                            <td>
                                ${e.warehouseName}
                            </td>

                            <td>
                                ${e.exportType}
                            </td>

                            <td>
                                ${e.receiverName}
                            </td>

                        </tr>

                    </c:forEach>


                    <c:if test="${empty reportExports}">

                        <tr>

                            <td colspan="5"
                                class="report-empty">

                                Không có dữ liệu xuất kho.

                            </td>

                        </tr>

                    </c:if>

                </tbody>

            </table>

        </div>

    </div>

</c:if>


<!-- ==================== BÁO CÁO HÀNG HƯ HỎNG ==================== -->

<c:if test="${selectedType == 'damaged'}">

    <div class="dashboard-card report-card">

        <div class="report-card-header">

            <div>

                <p class="report-eyebrow">
                    HƯ HỎNG
                </p>

                <h3>
                    Báo cáo hàng hư hỏng
                </h3>

                <span class="report-subtitle">
                    Các giao dịch xuất kho do hàng hư hỏng.
                </span>

            </div>

            <span class="report-count">
                ${empty reportDamaged ? 0 : reportDamaged.size()}
                giao dịch
            </span>

        </div>


        <div class="table-wrap">

            <table>

                <thead>

                    <tr>

                        <th>Thời gian</th>

                        <th>Kho</th>

                        <th>SKU</th>

                        <th>Sản phẩm</th>

                        <th class="text-right">
                            Số lượng hư hỏng
                        </th>

                        <th>Ghi chú</th>

                    </tr>

                </thead>


                <tbody>

                    <c:forEach items="${reportDamaged}" var="t">

                        <tr>

                            <td>
                                ${t.createdAt}
                            </td>

                            <td>
                                ${t.warehouseName}
                            </td>

                            <td>
                                <strong>
                                    ${t.sku}
                                </strong>
                            </td>

                            <td>

                                <strong>
                                    ${t.productName}
                                </strong>

                                <br>

                                <small>
                                    ${t.variantName}
                                </small>

                            </td>

                            <td class="text-right report-danger">

                                ${-t.quantityChange}

                            </td>

                            <td>
                                ${t.note}
                            </td>

                        </tr>

                    </c:forEach>


                    <c:if test="${empty reportDamaged}">

                        <tr>

                            <td colspan="6"
                                class="report-empty">

                                Không có dữ liệu hàng hư hỏng.

                            </td>

                        </tr>

                    </c:if>

                </tbody>

            </table>

        </div>

    </div>

</c:if>


<style>

    /* =========================================================
       REPORT PAGE
       ========================================================= */

    .warehouse-report-filter {
        margin-bottom: 18px;
        padding: 18px 20px;
    }

    .report-filter-grid {
        display: grid;
        grid-template-columns:
            minmax(180px, 1.2fr)
            minmax(150px, 1fr)
            minmax(150px, 1fr)
            minmax(200px, 1.2fr)
            auto;

        gap: 14px;

        align-items: end;
    }

    .report-filter-grid label {
        display: flex;
        flex-direction: column;
        gap: 7px;

        font-size: 13px;
        font-weight: 600;
    }

    .report-filter-grid input,
    .report-filter-grid select {
        width: 100%;
        min-height: 40px;

        box-sizing: border-box;
    }

    .report-filter-action {
        display: flex;
        align-items: flex-end;
    }

    .report-filter-action .button {
        min-height: 40px;
        white-space: nowrap;
    }


    /* =========================================================
       REPORT CARD
       ========================================================= */

    .report-card {
        margin-bottom: 18px;
        overflow: hidden;
    }

    .report-card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        gap: 16px;

        padding: 18px 20px;

        border-bottom: 1px solid #eeeeee;
    }

    .report-card-header h3 {
        margin: 2px 0 4px;
    }

    .report-eyebrow {
        margin: 0;

        font-size: 11px;
        font-weight: 700;

        letter-spacing: 0.08em;

        color: #9a7b32;
    }

    .report-subtitle {
        display: block;

        font-size: 12px;

        color: #777777;
    }

    .report-count {
        flex-shrink: 0;

        padding: 6px 10px;

        border: 1px solid #e5e5e5;
        border-radius: 6px;

        background: #fafafa;

        color: #666666;

        font-size: 12px;
        font-weight: 600;

        white-space: nowrap;
    }


    /* =========================================================
       TABLE
       ========================================================= */

    .report-card .table-wrap {
        overflow-x: auto;
    }

    .report-card table {
        width: 100%;
        min-width: 760px;

        border-collapse: collapse;
    }

    .report-card th {
        padding: 12px 14px;

        background: #fafafa;

        color: #555555;

        font-size: 12px;
        font-weight: 700;

        white-space: nowrap;

        border-bottom: 1px solid #e5e5e5;
    }

    .report-card td {
        padding: 13px 14px;

        color: #333333;

        font-size: 13px;

        border-bottom: 1px solid #f0f0f0;

        vertical-align: middle;
    }

    .report-card tbody tr:last-child td {
        border-bottom: none;
    }

    .report-card tbody tr:hover {
        background: #fcfcfc;
    }

    .report-card td strong {
        font-weight: 600;
    }

    .report-card td small {
        color: #777777;
        font-size: 12px;
    }


    /* =========================================================
       NUMBER / MONEY
       ========================================================= */

    .report-number,
    .text-right {
        text-align: right;
    }

    .report-highlight {
        font-weight: 700;
    }

    .report-money {
        font-weight: 600;
        white-space: nowrap;
    }

    .report-danger {
        color: #b42318;

        font-weight: 700;
    }


    /* =========================================================
       EMPTY STATE
       ========================================================= */

    .report-empty {
        padding: 36px 20px !important;

        text-align: center;

        color: #888888 !important;

        font-size: 13px !important;
    }


    /* =========================================================
       RESPONSIVE
       ========================================================= */

    @media (max-width: 1100px) {

        .report-filter-grid {
            grid-template-columns:
                repeat(2, minmax(180px, 1fr));
        }

        .report-filter-action {
            grid-column: 1 / -1;
        }

    }


    @media (max-width: 650px) {

        .report-filter-grid {
            grid-template-columns: 1fr;
        }

        .report-filter-action {
            grid-column: auto;
        }

        .report-filter-action .button {
            width: 100%;
        }

        .report-card-header {
            align-items: flex-start;
            flex-direction: column;
        }

    }

</style>